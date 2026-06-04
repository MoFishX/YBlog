package com.yvmoux.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yvmoux.blog.dto.request.ChangePasswordRequest;
import com.yvmoux.blog.dto.request.UpdateProfileRequest;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    @SaCheckLogin
    public Result<UserVO> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userService.getCurrentUser(userId));
    }

    @Operation(summary = "获取指定用户公开信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/me")
    @SaCheckLogin
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success("更新成功", userService.updateProfile(userId, request));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
    @SaCheckLogin
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }
}
