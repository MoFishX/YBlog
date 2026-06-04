package com.yvmoux.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.response.ApiResponse;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.PageResult;
import com.yvmoux.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文章")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章列表")
    @GetMapping
    public ApiResponse<PageResult<ArticleVO>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "latest") String orderBy) {
        log.debug("获取文章列表, page: {}, pageSize: {}, tagId: {}, orderBy: {}", page, pageSize, tagId, orderBy);
        if (pageSize > 100) pageSize = 100;
        return ApiResponse.success(articleService.getArticleList(page, pageSize, tagId, orderBy, null, null));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{articleId}")
    public ApiResponse<ArticleVO> getDetail(@PathVariable Long articleId) {
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ApiResponse.success(articleService.getArticleDetail(articleId, currentUserId));
    }

    @Operation(summary = "发布文章")
    @PostMapping
    @SaCheckLogin
    public ApiResponse<ArticleVO> create(@Valid @RequestBody ArticleCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ArticleVO article = articleService.createArticle(userId, request);
        return ApiResponse.success("发布成功", article);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{articleId}")
    @SaCheckLogin
    public ApiResponse<ArticleVO> update(@PathVariable Long articleId,
                                          @Valid @RequestBody ArticleUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ArticleVO article = articleService.updateArticle(articleId, userId, request);
        return ApiResponse.success("更新成功", article);
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{articleId}")
    @SaCheckLogin
    public ApiResponse<Void> delete(@PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        articleService.deleteArticle(articleId, userId, isAdmin);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{articleId}/like")
    @SaCheckLogin
    public ApiResponse<ArticleVO> like(@PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleService.toggleLike(articleId, userId));
    }

    @Operation(summary = "热门文章排行榜")
    @GetMapping("/hot")
    public ApiResponse<List<ArticleVO>> hot(@RequestParam(defaultValue = "10") Integer limit) {
        if (limit > 50) limit = 50;
        return ApiResponse.success(articleService.getHotArticles(limit));
    }

    @Operation(summary = "我的文章列表")
    @GetMapping("/mine")
    @SaCheckLogin
    public ApiResponse<PageResult<ArticleVO>> mine(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleService.getArticleList(page, pageSize, null, "latest", status, userId));
    }
}
