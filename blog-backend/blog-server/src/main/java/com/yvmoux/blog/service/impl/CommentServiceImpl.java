package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.request.CommentCreateRequest;
import com.yvmoux.blog.dto.response.CommentVO;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.entity.Comment;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.ArticleMapper;
import com.yvmoux.blog.mapper.CommentMapper;
import com.yvmoux.blog.mapper.UserMapper;
import com.yvmoux.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<CommentVO> getCommentsByArticle(Long articleId, Integer page, Integer pageSize) {
        // 校验文章存在
        if (articleMapper.selectById(articleId) == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 条件查询
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        wrapper.orderByAsc("created_at");

        // 分页查询
        Page<Comment> commentPage = commentMapper.selectPage(new Page<>(page, pageSize), wrapper);

        // 构建返回值
        List<CommentVO> vos = commentPage.getRecords().stream().map(comment -> CommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(buildAuthorVO(comment.getUserId()))
                .replyTo(comment.getParentId() != null ? buildAuthorVO(commentMapper.selectById(comment.getParentId()).getUserId()) : null)
                .articleId(comment.getArticleId())
                .isRead(comment.getIsRead() != null && comment.getIsRead() == 1)
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .build()).collect(Collectors.toList());

        return new PageResult<>(vos, commentPage.getTotal());
    }

    @Override
    public CommentVO createComment(Long userId, CommentCreateRequest request) {
        Long articleId = request.getArticleId();
        // 校验文章存在
        if (articleMapper.selectById(articleId) == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 校验父评论存在
        if (request.getParentId() != null && commentMapper.selectById(request.getParentId()) == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 插入评论
        Comment comment = Comment.builder()
                .articleId(articleId)
                .userId(userId)
                .content(request.getContent())
                .parentId(request.getParentId())
                .status("APPROVED")
                .isRead(0)
                .createdAt(LocalDateTime.now())
                .build();
        commentMapper.insert(comment);

        // 构建返回值
        return CommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        // 获取评论并校验权限
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        boolean isOwner = comment.getUserId().equals(userId);
        boolean isArticleAuthor = false;
        if (!isOwner) {
            Article article = articleMapper.selectById(comment.getArticleId());
            if (article != null && article.getAuthorId().equals(userId)) {
                isArticleAuthor = true;
            }
        }
        if (!isOwner && !isAdmin && !isArticleAuthor) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 删除评论
        commentMapper.deleteById(commentId);
    }

    @Override
    public PageResult<CommentVO> getReplies(Long userId, Integer page, Integer pageSize, boolean unreadOnly) {
        // 获取用户所有评论 ID
        QueryWrapper<Comment> userCommentsWrapper = new QueryWrapper<>();
        userCommentsWrapper.eq("user_id", userId);
        List<Comment> userComments = commentMapper.selectList(userCommentsWrapper);
        if (userComments.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0);
        }

        // 查询回复这些评论的内容
        List<Long> userCommentIds = userComments.stream().map(Comment::getId).collect(Collectors.toList());
        QueryWrapper<Comment> replyWrapper = new QueryWrapper<>();
        replyWrapper.in("parent_id", userCommentIds);
        if (unreadOnly) replyWrapper.eq("is_read", 0);
        replyWrapper.orderByDesc("created_at");

        return pageResult(page, pageSize, replyWrapper);
    }

    @Override
    public PageResult<CommentVO> getAllComments(Integer page, Integer pageSize, String keyword, Long articleId) {
        // 条件查询
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) wrapper.like("content", keyword);
        if (articleId != null) wrapper.eq("article_id", articleId);
        wrapper.orderByDesc("created_at");

        return pageResult(page, pageSize, wrapper);
    }

    @Override
    public void forceDeleteComment(Long commentId) {
        // 获取评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 强制删除
        commentMapper.deleteById(commentId);
    }

    private PageResult<CommentVO> pageResult(Integer page, Integer pageSize, QueryWrapper<Comment> replyWrapper) {
        // 分页查询
        Page<Comment> replyPage = commentMapper.selectPage(new Page<>(page, pageSize), replyWrapper);

        // 构建返回值
        List<CommentVO> vos = replyPage.getRecords().stream().map(reply -> CommentVO.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .user(buildAuthorVO(reply.getUserId()))
                .articleId(reply.getArticleId())
                .articleTitle(articleMapper.selectById(reply.getArticleId()).getTitle())
                .isRead(reply.getIsRead() != null && reply.getIsRead() == 1)
                .status(reply.getStatus())
                .createdAt(reply.getCreatedAt())
                .build()).collect(Collectors.toList());

        return new PageResult<>(vos, replyPage.getTotal());
    }

    private com.yvmoux.blog.dto.response.AuthorVO buildAuthorVO(Long userId) {
        if (userId == null) return null;
        com.yvmoux.blog.entity.User user = userMapper.selectById(userId);
        if (user == null) return null;
        return com.yvmoux.blog.dto.response.AuthorVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .build();
    }
}
