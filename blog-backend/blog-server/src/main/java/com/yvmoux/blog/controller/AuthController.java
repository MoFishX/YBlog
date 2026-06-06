package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.LoginResult;
import com.yvmoux.blog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录, username: {}", request.getUsername());
        LoginResult result = authService.login(request);
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
    public Result<LoginResult> refresh(@RequestBody String refreshToken) {
        //log.info("刷新Token, userId: {}", userId);
        LoginResult result = authService.refreshToken(refreshToken);
        //log.info("刷新Token成功, userId: {}", userId);
        return Result.success(result);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出");
        authService.logout(null);
        log.info("用户登出成功");
        return Result.success("已登出", null);
    }
}
