package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.request.CommentCreateRequest;
import com.yvmoux.blog.dto.response.CommentVO;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "评论")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "文章评论列表")
    @GetMapping("/articles/{articleId}/comments")
    public Result<PageResult<CommentVO>> getComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取文章评论列表, articleId: {}, page: {}, pageSize: {}", articleId, page, pageSize);
        if (pageSize > 100) pageSize = 100;
        Result<PageResult<CommentVO>> result = Result.success(commentService.getCommentsByArticle(articleId, page, pageSize));
        log.info("获取文章评论列表成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "发表评论")
    @PostMapping("/articles/{articleId}/comments")
    @PreAuthorize("isAuthenticated()")
    public Result<CommentVO> create(@PathVariable Long articleId,
                                    @Valid @RequestBody CommentCreateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("发表评论, userId: {}, articleId: {}", userId, articleId);
        CommentVO comment = commentService.createComment(userId, articleId, request);
        log.info("发表评论成功, commentId: {}", comment.getId());
        return Result.success("评论成功", comment);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable Long commentId) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        log.info("删除评论, userId: {}, commentId: {}, isAdmin: {}", userId, commentId, isAdmin);
        commentService.deleteComment(commentId, userId, isAdmin);
        log.info("删除评论成功, commentId: {}", commentId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "我收到的评论回复")
    @GetMapping("/comments/replies")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<CommentVO>> replies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "0") Integer unreadOnly) {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("获取评论回复, userId: {}, page: {}, pageSize: {}, unreadOnly: {}", userId, page, pageSize, unreadOnly);
        Result<PageResult<CommentVO>> result = Result.success(commentService.getReplies(userId, page, pageSize, unreadOnly == 1));
        log.info("获取评论回复成功, total: {}", result.getData().getTotal());
        return result;
    }
}
