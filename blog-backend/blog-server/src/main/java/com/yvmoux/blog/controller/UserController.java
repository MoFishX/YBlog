package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.request.ChangePasswordRequest;
import com.yvmoux.blog.dto.request.UpdateProfileRequest;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "用户")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
//    @SaCheckLogin
    public Result<UserVO> getCurrentUser() {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("获取当前用户信息, userId: {}", userId);
        Result<UserVO> result = Result.success(userService.getCurrentUser(userId));
        log.info("获取当前用户信息成功, userId: {}", userId);
        return result;
    }

    @Operation(summary = "获取指定用户公开信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        log.info("获取用户公开信息, userId: {}", userId);
        Result<UserVO> result = Result.success(userService.getUserById(userId));
        log.info("获取用户公开信息成功, userId: {}", userId);
        return result;
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/me")
//    @SaCheckLogin
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("更新个人资料, userId: {}", userId);
        Result<UserVO> result = Result.success("更新成功", userService.updateProfile(userId, request));
        log.info("更新个人资料成功, userId: {}", userId);
        return result;
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
//    @SaCheckLogin
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("修改密码, userId: {}", userId);
        userService.changePassword(userId, request);
        log.info("修改密码成功, userId: {}", userId);
        return Result.success("密码修改成功", null);
    }
}
