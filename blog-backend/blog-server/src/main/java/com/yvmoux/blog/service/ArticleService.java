package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.PageResult;

public interface ArticleService {
    PageResult<ArticleVO> getArticleList(Integer page, Integer pageSize, Long tagId, String orderBy, String status, Long userId);
    ArticleVO getArticleDetail(Long articleId, Long currentUserId);
    ArticleVO createArticle(Long userId, ArticleCreateRequest request);
    ArticleVO updateArticle(Long articleId, Long userId, ArticleUpdateRequest request);
    void deleteArticle(Long articleId, Long userId, boolean isAdmin);
    ArticleVO toggleLike(Long articleId, Long userId);
    PageResult<ArticleVO> getHotArticles(int limit);
    void batchDelete(java.util.List<Long> ids);
    ArticleVO reviewArticle(Long articleId, String status, String reason);
    PageResult<ArticleVO> getAllArticles(Integer page, Integer pageSize, String status, String keyword);
}
