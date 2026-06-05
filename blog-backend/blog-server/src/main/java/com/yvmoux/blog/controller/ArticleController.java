package com.yvmoux.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.enums.ArticleStatusEnum;
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
    public Result<PageResult<ArticleVO>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String tagName,
            @RequestParam(defaultValue = "latest") String orderBy) {
        log.info("获取文章列表, page: {}, pageSize: {}, tagName: {}, orderBy: {}", page, pageSize, tagName, orderBy);
        if (pageSize > 100) pageSize = 100;
        return Result.success(articleService.getArticleList(page, pageSize, null, orderBy, ArticleStatusEnum.PUBLISHED.name(), null));
    }

    @Operation(summary = "热门文章排行榜")
    @GetMapping("/hot")
    public Result<List<ArticleVO>> hot(@RequestParam(defaultValue = "10") Integer limit) {
        if (limit > 50) limit = 50;
        return Result.success(articleService.getHotArticles(limit));
    }

    @Operation(summary = "我的文章列表")
    @GetMapping("/mine")
    @SaCheckLogin
    public Result<PageResult<ArticleVO>> mine(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(articleService.getArticleList(page, pageSize, null, "latest", status, userId));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{articleId}")
    public Result<ArticleVO> getDetail(@PathVariable Long articleId) {
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return Result.success(articleService.getArticleDetail(articleId));
    }

    @Operation(summary = "发布文章")
    @PostMapping
    @SaCheckLogin
    public Result<ArticleVO> create(@Valid @RequestBody ArticleCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ArticleVO article = articleService.createArticle(userId, request);
        return Result.success("发布成功", article);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{articleId}")
    @SaCheckLogin
    public Result<ArticleVO> update(@PathVariable Long articleId,
                                    @Valid @RequestBody ArticleUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ArticleVO article = articleService.updateArticle(articleId, userId, request);
        return Result.success("更新成功", article);
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{articleId}")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        articleService.deleteArticle(articleId, userId, isAdmin);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{articleId}/like")
    @SaCheckLogin
    public Result<ArticleVO> like(@PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(articleService.toggleLike(articleId, userId));
    }
}
