package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.request.ChangePasswordRequest;
import com.yvmoux.blog.dto.request.UpdateProfileRequest;
import com.yvmoux.blog.dto.response.ApiResponse;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserVO> getCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();
        return ApiResponse.success(userService.getCurrentUser(userId));
    }

    @Operation(summary = "获取指定用户公开信息")
    @GetMapping("/{userId}")
    public ApiResponse<UserVO> getUserById(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUserById(userId));
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ApiResponse.success("更新成功", userService.updateProfile(userId, request));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        userService.changePassword(userId, request);
        return ApiResponse.success("密码修改成功", null);
    }
}
