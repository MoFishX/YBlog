package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.request.CommentCreateRequest;
import com.yvmoux.blog.dto.response.CommentVO;

public interface CommentService {
    PageResult<CommentVO> getCommentsByArticle(Long articleId, Long userId, Integer page, Integer pageSize);
    CommentVO createComment(Long userId, CommentCreateRequest request);
    void deleteComment(Long commentId, Long userId, boolean isAdmin);
    void hideComment(Long commentId);
    PageResult<CommentVO> getReplies(Long userId, Integer page, Integer pageSize, boolean unreadOnly);
    PageResult<CommentVO> getMyComments(Long userId, Integer page, Integer pageSize);
    PageResult<CommentVO> getAllComments(Integer page, Integer pageSize, String keyword, Long articleId);
    void forceDeleteComment(Long commentId);
}