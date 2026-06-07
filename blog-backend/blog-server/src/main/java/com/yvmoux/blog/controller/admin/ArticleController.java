package com.yvmoux.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理-文章")
@RestController("adminArticleController")
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "所有文章列表")
    @GetMapping
    public Result<PageResult<ArticleVO>> articleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        log.info("获取全部文章列表, page: {}, pageSize: {}, status: {}, keyword: {}", page, pageSize, status, keyword);
        Result<PageResult<ArticleVO>> result = Result.success(articleService.getAllArticles(page, pageSize, status, keyword));
        log.info("获取全部文章列表成功, total: {}", result.getData().getTotal());
        return result;
    }

    @Operation(summary = "强制删除文章")
    @DeleteMapping
    public Result<Void> forceDeleteArticle(@RequestParam String ids) {
        log.info("强制删除文章, articleId: {}", ids);
        articleService.deleteArticle(ids, null, true);
        log.info("强制删除文章成功, articleId: {}", ids);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "生成AI总结")
    @PostMapping("/{articleId}/summary")
    public Result<Void> generateSummary(@PathVariable Long articleId) {
        log.info("手动生成AI总结, articleId: {}", articleId);
        articleService.triggerAiSummary(articleId);
        return Result.success("已提交AI总结任务", null);
    }

    @Operation(summary = "删除AI总结")
    @DeleteMapping("/{articleId}/summary")
    public Result<Void> deleteSummary(@PathVariable Long articleId) {
        log.info("删除AI总结, articleId: {}", articleId);
        articleService.deleteAiSummary(articleId);
        return Result.success("已删除AI总结", null);
    }
}
