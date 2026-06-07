package com.yvmoux.blog.controller.pub;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.enums.ArticleStatusEnum;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公共-文章")
@RestController("pubArticleController")
@RequestMapping("/articles")
@RequiredArgsConstructor
@Slf4j
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

    @Operation(summary = "搜索文章")
    @GetMapping("/search")
    public Result<PageResult<ArticleVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("搜索文章, keyword: {}, page: {}, pageSize: {}", keyword, page, pageSize);
        if (pageSize > 50) pageSize = 50;
        Result<PageResult<ArticleVO>> result = Result.success(articleService.search(keyword, page, pageSize));
        log.info("搜索文章成功, total: {}", result.getData().getTotal());
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
}
