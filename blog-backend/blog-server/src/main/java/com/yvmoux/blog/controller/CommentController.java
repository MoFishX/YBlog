package com.yvmoux.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yvmoux.blog.dto.request.CommentCreateRequest;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.CommentVO;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "文章评论列表")
    @GetMapping("/articles/{articleId}/comments")
    public Result<PageResult<CommentVO>> getComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        if (pageSize > 100) pageSize = 100;
        return Result.success(commentService.getCommentsByArticle(articleId, page, pageSize));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/articles/{articleId}/comments")
    @SaCheckLogin
    public Result<CommentVO> create(@PathVariable Long articleId,
                                    @Valid @RequestBody CommentCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommentVO comment = commentService.createComment(userId, articleId, request);
        return Result.success("评论成功", comment);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{commentId}")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        commentService.deleteComment(commentId, userId, isAdmin);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "我收到的评论回复")
    @GetMapping("/comments/replies")
    @SaCheckLogin
    public Result<PageResult<CommentVO>> replies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "0") Integer unreadOnly) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(commentService.getReplies(userId, page, pageSize, unreadOnly == 1));
    }
}
