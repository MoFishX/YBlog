package com.yvmoux.blog.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.yvmoux.blog.dto.request.BatchDeleteRequest;
import com.yvmoux.blog.dto.request.ReviewRequest;
import com.yvmoux.blog.dto.request.UpdateUserRoleRequest;
import com.yvmoux.blog.dto.request.UpdateUserStatusRequest;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.CommentVO;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.response.StatsVO;
import com.yvmoux.blog.dto.response.TrendVO;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.service.AdminService;
import com.yvmoux.blog.service.ArticleService;
import com.yvmoux.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理后台")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
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
        return Result.success(adminService.getUserList(page, pageSize, keyword));
    }

    @Operation(summary = "封禁/解封用户")
    @PutMapping("/users/{userId}/status")
    public Result<UserVO> updateUserStatus(@PathVariable Long userId,
                                           @Valid @RequestBody UpdateUserStatusRequest request) {
        adminService.updateUserStatus(userId, request.getStatus());
        UserVO vo = new UserVO();
        vo.setId(userId);
        vo.setStatus(request.getStatus());
        return Result.success("操作成功", vo);
    }

    @Operation(summary = "修改用户角色")
    @PutMapping("/users/{userId}/role")
    public Result<UserVO> updateUserRole(@PathVariable Long userId,
                                         @Valid @RequestBody UpdateUserRoleRequest request) {
        adminService.updateUserRole(userId, request.getRole());
        UserVO vo = new UserVO();
        vo.setId(userId);
        vo.setRole(request.getRole());
        return Result.success("修改成功", vo);
    }

    @Operation(summary = "所有文章列表")
    @GetMapping("/articles")
    public Result<PageResult<ArticleVO>> articleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.success(articleService.getAllArticles(page, pageSize, status, keyword));
    }

    @Operation(summary = "强制删除文章")
    @DeleteMapping("/articles/{articleId}")
    public Result<Void> forceDeleteArticle(@PathVariable Long articleId) {
        articleService.deleteArticle(articleId, null, true);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "批量删除文章")
    @PostMapping("/articles/batch-delete")
    public Result<Map<String, Integer>> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        articleService.batchDelete(request.getIds());
        return Result.success("已删除 " + request.getIds().size() + " 篇文章",
                java.util.Map.of("deletedCount", request.getIds().size()));
    }

    @Operation(summary = "审核文章")
    @PutMapping("/articles/{articleId}/review")
    public Result<ArticleVO> review(@PathVariable Long articleId,
                                    @Valid @RequestBody ReviewRequest request) {
        ArticleVO article = articleService.reviewArticle(articleId, request.getStatus(), request.getReason());
        String msg = "APPROVED".equals(request.getStatus()) ? "审核通过" : "已驳回";
        return Result.success(msg, article);
    }

    @Operation(summary = "评论管理列表")
    @GetMapping("/comments")
    public Result<PageResult<CommentVO>> commentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long articleId) {
        return Result.success(commentService.getAllComments(page, pageSize, keyword, articleId));
    }

    @Operation(summary = "强制删除评论")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> forceDeleteComment(@PathVariable Long commentId) {
        commentService.forceDeleteComment(commentId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "系统统计概览")
    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        return Result.success(adminService.getStats());
    }

    @Operation(summary = "周访问趋势")
    @GetMapping("/stats/weekly-trend")
    public Result<List<TrendVO>> weeklyTrend() {
        return Result.success(adminService.getWeeklyTrend());
    }
}
