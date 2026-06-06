package com.yvmoux.blog.config;

import com.yvmoux.blog.filter.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity //开启 URL 级安全
@EnableMethodSecurity //开启 方法级细粒度权限
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // 1. 跨域
        http.cors(Customizer.withDefaults());

        // 2. 禁用 CSRF（API 无状态场景） 表单登录页 登录框
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 3. 无状态会话（不使用 Session）
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4. 权限配置
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/auth/**",
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**",
                        "/articles", "/articles/*", "/articles/hot",
                        "/articles/*/comments", "/articles/*/view",
                        "/user/*", "/tags"
                ).permitAll()
                // 后台管理需要 ADMIN 角色
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 剩余需要登录
                .anyRequest().authenticated()
        );

        // 5. 异常处理
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                    response.getWriter().write("{\"code\":403,\"message\":\"无权限\"}");
                })
        );

        // 6. JWT过滤器（在 UsernamePasswordAuthenticationFilter 之前
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
