package com.yvmoux.blog.controller.pub;

import com.yvmoux.blog.dto.LoginResult;
import com.yvmoux.blog.dto.RefreshResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "公共-认证")
@RestController("pubAuthController")
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 3600;

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginResult> login(@Valid @RequestBody LoginRequest request,
                                     HttpServletResponse response) {
        log.info("用户登录, username: {}", request.getUsername());
        LoginResult result = authService.login(request);

        if (result.getRefreshToken() != null) {
            addRefreshTokenCookie(response, result.getRefreshToken());
        }

        log.info("用户登录成功, userId: {}", result.getUser().getId());
        return Result.success(result);
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册, username: {}", request.getUsername());
        authService.register(request);
        log.info("用户注册成功, username: {}", request.getUsername());
        return Result.success("注册成功", null);
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<RefreshResult> refresh(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        if (refreshToken == null) {
            return Result.error(401, "缺少刷新令牌");
        }
        log.info("刷新Token");
        RefreshResult result = authService.refreshToken(refreshToken);
        log.info("刷新Token成功");
        return Result.success(result);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                               HttpServletResponse response) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        removeRefreshTokenCookie(response);
        log.info("用户登出成功");
        return Result.success("已登出", null);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void removeRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }
}
