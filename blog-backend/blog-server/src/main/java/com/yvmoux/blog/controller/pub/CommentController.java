package com.yvmoux.blog.controller.pub;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.CommentVO;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "公共-评论")
@RestController("pubCommentController")
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "文章评论列表")
    @GetMapping
    public Result<PageResult<CommentVO>> getComments(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取文章评论列表, articleId: {}, page: {}, pageSize: {}", articleId, page, pageSize);
        if (pageSize > 100) pageSize = 100;
        Long userId = securityUtils.getCurrentUserId();
        Result<PageResult<CommentVO>> result = Result.success(commentService.getCommentsByArticle(articleId, userId, page, pageSize));
        log.info("获取文章评论列表成功, total: {}", result.getData().getTotal());
        return result;
    }
}