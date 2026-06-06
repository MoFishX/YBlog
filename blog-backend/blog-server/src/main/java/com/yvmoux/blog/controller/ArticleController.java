package com.yvmoux.blog.controller;

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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "文章")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
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
        Result<PageResult<ArticleVO>> result = Result.success(articleService.getArticleList(page, pageSize, null, orderBy, ArticleStatusEnum.PUBLISHED.name(), null));
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
//    @SaCheckLogin
    public Result<PageResult<ArticleVO>> mine(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("获取我的文章列表, userId: {}, page: {}, pageSize: {}, status: {}", userId, page, pageSize, status);
        Result<PageResult<ArticleVO>> result = Result.success(articleService.getArticleList(page, pageSize, null, "latest", status, userId));
        log.info("获取我的文章列表成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{articleId}")
    public Result<ArticleVO> getDetail(@PathVariable Long articleId) {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("获取文章详情, articleId: {}", articleId);
        Result<ArticleVO> result = Result.success(articleService.getArticleDetail(articleId, userId));
        log.info("获取文章详情成功, articleId: {}", articleId);
        return result;
    }

    @Operation(summary = "发布文章")
    @PostMapping
//    @SaCheckLogin
    public Result<ArticleVO> create(@Valid @RequestBody ArticleCreateRequest request) {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("发布文章, userId: {}, title: {}", userId, request.getTitle());
        ArticleVO article = articleService.createArticle(userId, request);
        log.info("发布文章成功, articleId: {}", article.getId());
        return Result.success("发布成功", article);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{articleId}")
//    @SaCheckLogin
    public Result<ArticleVO> update(@PathVariable Long articleId,
                                    @Valid @RequestBody ArticleUpdateRequest request) {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("更新文章, userId: {}, articleId: {}", userId, articleId);
        ArticleVO article = articleService.updateArticle(articleId, userId, request);
        log.info("更新文章成功, articleId: {}", article.getId());
        return Result.success("更新成功", article);
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{articleId}")
//    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long articleId) {
//        Long userId = StpKit.getLoginId();
//        boolean isAdmin = StpKit.isAdmin();
        Long userId = 1L;
        boolean isAdmin = true;
        log.info("删除文章, userId: {}, articleId: {}, isAdmin: {}", userId, articleId, isAdmin);
        articleService.deleteArticle(articleId, userId, isAdmin);
        log.info("删除文章成功, articleId: {}", articleId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{articleId}/like")
//    @SaCheckLogin
    public Result<ArticleVO> like(@PathVariable Long articleId) {
//        Long userId = StpKit.getLoginId();
        Long userId = 1L;
        log.info("点赞/取消点赞, userId: {}, articleId: {}", userId, articleId);
        Result<ArticleVO> result = Result.success(articleService.toggleLike(articleId, userId));
        log.info("点赞/取消点赞成功, isLiked: {}", result.getData().getIsLiked());
        return result;
    }
}
