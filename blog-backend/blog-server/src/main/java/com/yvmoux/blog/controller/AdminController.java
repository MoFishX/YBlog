package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.request.UpdateUserRoleRequest;
import com.yvmoux.blog.dto.request.UpdateUserStatusRequest;
import com.yvmoux.blog.dto.response.*;
import com.yvmoux.blog.service.AdminService;
import com.yvmoux.blog.service.ArticleService;
import com.yvmoux.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "管理后台")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ArticleService articleService;
    private final CommentService commentService;

    @Operation(summary = "用户列表")
    @GetMapping("/users")
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
    @PutMapping("/users/{userId}/status")
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
    @PutMapping("/users/{userId}/role")
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

    @Operation(summary = "所有文章列表")
    @GetMapping("/articles")
    public Result<PageResult<ArticleVO>> articleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        log.info("获取全部文章列表, page: {}, pageSize: {}, status: {}, keyword: {}", page, pageSize, status, keyword);
        Result<PageResult<ArticleVO>> result = Result.success(articleService.getAllArticles(page, pageSize, status, keyword));
        log.info("获取全部文章列表成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "强制删除文章")
    @DeleteMapping("/articles")
    public Result<Void> forceDeleteArticle(@RequestParam String ids) {
        log.info("强制删除文章, articleId: {}", ids);
        articleService.deleteArticle(ids, null, true);
        log.info("强制删除文章成功, articleId: {}", ids);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "评论管理列表")
    @GetMapping("/comments")
    public Result<PageResult<CommentVO>> commentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long articleId) {
        log.info("获取评论管理列表, page: {}, pageSize: {}, keyword: {}, articleId: {}", page, pageSize, keyword, articleId);
        Result<PageResult<CommentVO>> result = Result.success(commentService.getAllComments(page, pageSize, keyword, articleId));
        log.info("获取评论管理列表成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "强制删除评论")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> forceDeleteComment(@PathVariable Long commentId) {
        log.info("强制删除评论, commentId: {}", commentId);
        commentService.forceDeleteComment(commentId);
        log.info("强制删除评论成功, commentId: {}", commentId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "系统统计概览")
    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        log.info("获取系统统计概览");
        Result<StatsVO> result = Result.success(adminService.getStats());
        log.info("获取系统统计概览成功");
        return result;
    }

    @Operation(summary = "周访问趋势")
    @GetMapping("/stats/weekly-trend")
    public Result<List<TrendVO>> weeklyTrend() {
        log.info("获取周访问趋势");
        Result<List<TrendVO>> result = Result.success(adminService.getWeeklyTrend());
        log.info("获取周访问趋势成功, days: {}", result.getData().size());
        return result;
    }
}
