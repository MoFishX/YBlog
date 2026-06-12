package com.yvmoux.blog.controller.user;

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

@Tag(name = "用户-评论")
@RestController("userCommentController")
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<CommentVO> create(@Valid @RequestBody CommentCreateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("发表评论: {}", request);
        CommentVO comment = commentService.createComment(userId, request);
        log.info("发表评论成功, commentId: {}", comment.getContent());
        return Result.success("评论成功", comment);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public Result<Void> delete(@PathVariable Long commentId) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        log.info("删除评论, userId: {}, commentId: {}, isAdmin: {}", userId, commentId, isAdmin);
        commentService.deleteComment(commentId, userId, isAdmin);
        log.info("删除评论成功, commentId: {}", commentId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "我收到的评论回复")
    @GetMapping("/replies")
    public Result<PageResult<CommentVO>> replies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "0") Integer unreadOnly) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("获取评论回复, userId: {}, page: {}, pageSize: {}, unreadOnly: {}", userId, page, pageSize, unreadOnly);
        Result<PageResult<CommentVO>> result = Result.success(commentService.getReplies(userId, page, pageSize, unreadOnly == 1));
        log.info("获取评论回复成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "我发表的评论")
    @GetMapping("/mine")
    public Result<PageResult<CommentVO>> myComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = securityUtils.getCurrentUserId();
        return Result.success(commentService.getMyComments(userId, page, pageSize));
    }
}