package com.yvmoux.blog.controller.admin;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.CommentVO;
import com.yvmoux.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理-评论")
@RestController("adminCommentController")
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "评论管理列表")
    @GetMapping
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
    @DeleteMapping("/{commentId}")
    public Result<Void> forceDeleteComment(@PathVariable Long commentId) {
        log.info("强制删除评论, commentId: {}", commentId);
        commentService.forceDeleteComment(commentId);
        log.info("强制删除评论成功, commentId: {}", commentId);
        return Result.success("删除成功", null);
    }
}
