package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.request.CommentCreateRequest;
import com.yvmoux.blog.dto.response.CommentVO;
import com.yvmoux.blog.dto.PageResult;

public interface CommentService {
    PageResult<CommentVO> getCommentsByArticle(Long articleId, Integer page, Integer pageSize);
    CommentVO createComment(Long userId, Long articleId, CommentCreateRequest request);
    void deleteComment(Long commentId, Long userId, boolean isAdmin);
    PageResult<CommentVO> getReplies(Long userId, Integer page, Integer pageSize, boolean unreadOnly);
    PageResult<CommentVO> getAllComments(Integer page, Integer pageSize, String keyword, Long articleId);
    void forceDeleteComment(Long commentId);
}
