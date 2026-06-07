package com.yvmoux.blog.controller.user;

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

@Tag(name = "用户-文章")
@RestController("userArticleController")
@RequestMapping("/articles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class ArticleController {

    private final ArticleService articleService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "我的文章列表")
    @GetMapping("/mine")
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

    @Operation(summary = "发布文章")
    @PostMapping
    public Result<ArticleVO> create(@Valid @RequestBody ArticleCreateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("发布文章, userId: {}, title: {}", userId, request.getTitle());
        ArticleVO article = articleService.createArticle(userId, request);
        log.info("发布文章成功, articleId: {}", article.getId());
        return Result.success("发布成功", article);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{articleId}")
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
    public Result<Void> delete(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("删除文章, userId: {}, articleId: {}", userId, articleId);
        articleService.deleteArticleOne(articleId, userId);
        log.info("删除文章成功, articleId: {}", articleId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{articleId}/like")
    public Result<ArticleVO> like(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("点赞/取消点赞, userId: {}, articleId: {}", userId, articleId);
        Result<ArticleVO> result = Result.success(articleService.toggleLike(articleId, userId));
        log.info("点赞/取消点赞成功, isLiked: {}", result.getData().getIsLiked());
        return result;
    }
}
