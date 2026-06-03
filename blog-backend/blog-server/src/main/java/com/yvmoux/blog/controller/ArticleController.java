package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleQueryRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.response.ApiResponse;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.PageResult;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文章")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "文章列表")
    @GetMapping
    public ApiResponse<PageResult<ArticleVO>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "latest") String orderBy) {
        if (pageSize > 100) pageSize = 100;
        return ApiResponse.success(articleService.getArticleList(page, pageSize, tagId, orderBy, null, null));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{articleId}")
    public ApiResponse<ArticleVO> getDetail(@PathVariable Long articleId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        return ApiResponse.success(articleService.getArticleDetail(articleId, currentUserId));
    }

    @Operation(summary = "发布文章")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ArticleVO> create(@Valid @RequestBody ArticleCreateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        ArticleVO article = articleService.createArticle(userId, request);
        return ApiResponse.success("发布成功", article);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ArticleVO> update(@PathVariable Long articleId,
                                          @Valid @RequestBody ArticleUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        ArticleVO article = articleService.updateArticle(articleId, userId, request);
        return ApiResponse.success("更新成功", article);
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        articleService.deleteArticle(articleId, userId, isAdmin);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{articleId}/like")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ArticleVO> like(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        return ApiResponse.success(articleService.toggleLike(articleId, userId));
    }

    @Operation(summary = "热门文章排行榜")
    @GetMapping("/hot")
    public ApiResponse<PageResult<ArticleVO>> hot(@RequestParam(defaultValue = "10") Integer limit) {
        if (limit > 50) limit = 50;
        return ApiResponse.success(articleService.getHotArticles(limit));
    }

    @Operation(summary = "我的文章列表")
    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResult<ArticleVO>> mine(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long userId = securityUtils.getCurrentUserId();
        return ApiResponse.success(articleService.getArticleList(page, pageSize, null, "latest", status, userId));
    }
}
