package com.yvmoux.blog.filter;

import com.yvmoux.blog.constant.JwtConstants;
import com.yvmoux.blog.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * <p>
 * JWT 过滤器是前后端分离认证的核心组件，用于拦截所有请求，自动解析请求头中的 Token，
 * 完成用户身份认证，无需手动登录，实现无状态认证。
 * </p>
 * <p>
 * 执行流程：
 * <ol>
 *     <li>拦截所有 HTTP 请求</li>
 *     <li>判断请求头是否携带 Token</li>
 *     <li>校验 Token 合法性及是否过期</li>
 *     <li>解析用户名，查询用户信息并存入安全上下文</li>
 *     <li>放行请求，后续权限拦截器可直接获取登录用户信息</li>
 * </ol>
 * </p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(JwtConstants.AUTH_HEADER);
        if (header == null || !header.startsWith(JwtConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(JwtConstants.BEARER_PREFIX.length());
        String username = jwtUtils.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtils.validateToken(token)) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
