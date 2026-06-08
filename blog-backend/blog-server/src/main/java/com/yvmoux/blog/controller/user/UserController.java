package com.yvmoux.blog.controller.user;

import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.request.ChangePasswordRequest;
import com.yvmoux.blog.dto.request.UpdateProfileRequest;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户")
@RestController("userUserController")
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "获取指定用户信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        log.info("获取用户公开信息, userId: {}", userId);
        Result<UserVO> result = Result.success(userService.getUserById(userId));
        log.info("获取用户公开信息成功, userId: {}", userId);
        return result;
    }

    @GetMapping("/info")
    @Operation(summary = "用户信息")
    public Result<UserVO> info() {
        log.info("获取用户信息");
        Long currentUserId = securityUtils.getCurrentUserId();
        UserVO userVO = userService.getUserById(currentUserId);
        log.info("获取用户信息成功, userId: {}", userVO.getId());
        return Result.success(userVO);
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/me")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("更新个人资料, userId: {}", userId);
        Result<UserVO> result = Result.success("更新成功", userService.updateProfile(userId, request));
        log.info("更新个人资料成功, userId: {}", userId);
        return result;
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("修改密码, userId: {}", userId);
        userService.changePassword(userId, request);
        log.info("修改密码成功, userId: {}", userId);
        return Result.success("密码修改成功", null);
    }
}
