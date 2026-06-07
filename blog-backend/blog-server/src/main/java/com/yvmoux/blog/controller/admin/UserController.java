package com.yvmoux.blog.controller.admin;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.request.UpdateUserRoleRequest;
import com.yvmoux.blog.dto.request.UpdateUserStatusRequest;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理-用户")
@RestController("adminUserController")
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final AdminService adminService;

    @Operation(summary = "用户列表")
    @GetMapping
    public Result<PageResult<UserVO>> userList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("获取用户列表, page: {}, pageSize: {}, keyword: {}", page, pageSize, keyword);
        Result<PageResult<UserVO>> result = Result.success(adminService.getUserList(page, pageSize, keyword));
        log.info("获取用户列表成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "封禁/解封用户")
    @PutMapping("/{userId}/status")
    public Result<UserVO> updateUserStatus(@PathVariable Long userId,
                                           @Valid @RequestBody UpdateUserStatusRequest request) {
        log.info("修改用户状态, userId: {}, status: {}", userId, request.getStatus());
        adminService.updateUserStatus(userId, request.getStatus());
        UserVO vo = new UserVO();
        vo.setId(userId);
        vo.setStatus(request.getStatus());
        log.info("修改用户状态成功, userId: {}, status: {}", userId, request.getStatus());
        return Result.success("操作成功", vo);
    }

    @Operation(summary = "修改用户角色")
    @PutMapping("/{userId}/role")
    public Result<UserVO> updateUserRole(@PathVariable Long userId,
                                         @Valid @RequestBody UpdateUserRoleRequest request) {
        log.info("修改用户角色, userId: {}, role: {}", userId, request.getRole());
        adminService.updateUserRole(userId, request.getRole());
        UserVO vo = new UserVO();
        vo.setId(userId);
        vo.setRole(request.getRole());
        log.info("修改用户角色成功, userId: {}, role: {}", userId, request.getRole());
        return Result.success("修改成功", vo);
    }
}
