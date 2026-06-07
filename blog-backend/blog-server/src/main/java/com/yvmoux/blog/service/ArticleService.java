package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.response.ArticleVO;

import java.util.List;

public interface ArticleService {
    /**
     * 获取文章列表
     */
    PageResult<ArticleVO> getArticleList(Integer page, Integer pageSize, String tagName, String orderBy, String status, Long authorId);
    /**
     * 获取文章详情
     */
    ArticleVO getArticleDetail(Long articleId, Long currentUserId);
    /**
     * 创建文章
     */
    ArticleVO createArticle(Long userId, ArticleCreateRequest request);
    /**
     * 更新文章
     */
    ArticleVO updateArticle(Long articleId, Long userId, ArticleUpdateRequest request);
    /**
     * 删除文章
     */
    void deleteArticle(Long articleId, Long userId, boolean isAdmin);
    /**
     * 点赞文章
     */
    ArticleVO toggleLike(Long articleId, Long userId);
    /**
     * 获取最热文章
     */
    List<ArticleVO> getHotArticles(int limit);
    /**
     * 批量删除文章
     */
    void batchDelete(java.util.List<Long> ids);
    /**
     * 获取所有文章列表
     */
    PageResult<ArticleVO> getAllArticles(Integer page, Integer pageSize, String status, String keyword);
}
