package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.enums.ArticleStatusEnum;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "文章")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "文章列表（支持按作者、标签、排序筛选）")
    @GetMapping
    public Result<PageResult<ArticleVO>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "latest") String orderBy) {
        log.info("获取文章列表, page: {}, pageSize: {}, tagName: {}, authorId: {}, orderBy: {}", page, pageSize, tagName, authorId, orderBy);
        if (pageSize > 100) pageSize = 100;
        Result<PageResult<ArticleVO>> result = Result.success(articleService.getArticleList(page, pageSize, tagName, orderBy, ArticleStatusEnum.PUBLISHED.name(), authorId));
        log.info("获取文章列表成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "热门文章排行榜")
    @GetMapping("/hot")
    public Result<List<ArticleVO>> hot(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门文章排行榜, limit: {}", limit);
        if (limit > 50) limit = 50;
        Result<List<ArticleVO>> result = Result.success(articleService.getHotArticles(limit));
        log.info("获取热门文章排行榜成功, count: {}", result.getData().size());
        return result;
    }

    @Operation(summary = "我的文章列表")
    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<ArticleVO>> mine(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("获取我的文章列表, userId: {}, page: {}, pageSize: {}, status: {}", userId, page, pageSize, status);
        Result<PageResult<ArticleVO>> result = Result.success(articleService.getArticleList(page, pageSize, null, "latest", status, userId));
        log.info("获取我的文章列表成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{articleId}")
    public Result<ArticleVO> getDetail(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("获取文章详情, articleId: {}", articleId);
        Result<ArticleVO> result = Result.success(articleService.getArticleDetail(articleId, userId));
        log.info("获取文章详情成功, articleId: {}", articleId);
        return result;
    }

    @Operation(summary = "发布文章")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<ArticleVO> create(@Valid @RequestBody ArticleCreateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("发布文章, userId: {}, title: {}", userId, request.getTitle());
        ArticleVO article = articleService.createArticle(userId, request);
        log.info("发布文章成功, articleId: {}", article.getId());
        return Result.success("发布成功", article);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public Result<ArticleVO> update(@PathVariable Long articleId,
                                    @Valid @RequestBody ArticleUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("更新文章, userId: {}, articleId: {}", userId, articleId);
        ArticleVO article = articleService.updateArticle(articleId, userId, request);
        log.info("更新文章成功, articleId: {}", article.getId());
        return Result.success("更新成功", article);
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        log.info("删除文章, userId: {}, articleId: {}, isAdmin: {}", userId, articleId, isAdmin);
        articleService.deleteArticle(articleId, userId, isAdmin);
        log.info("删除文章成功, articleId: {}", articleId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{articleId}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<ArticleVO> like(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("点赞/取消点赞, userId: {}, articleId: {}", userId, articleId);
        Result<ArticleVO> result = Result.success(articleService.toggleLike(articleId, userId));
        log.info("点赞/取消点赞成功, isLiked: {}", result.getData().getIsLiked());
        return result;
    }
}
