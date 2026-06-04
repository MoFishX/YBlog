package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yvmoux.blog.dto.request.CommentCreateRequest;
import com.yvmoux.blog.dto.response.AuthorVO;
import com.yvmoux.blog.dto.response.CommentVO;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.entity.Comment;
import com.yvmoux.blog.entity.User;
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
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        wrapper.orderByAsc("created_at");

        Page<Comment> commentPage = new Page<>(page, pageSize);
        Page<Comment> result = commentMapper.selectPage(commentPage, wrapper);

        List<CommentVO> vos = result.getRecords().stream().map(comment -> {
            AuthorVO userVO = buildAuthorVO(comment.getUserId());
            AuthorVO replyToVO = null;
            if (comment.getParentId() != null) {
                Comment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null) {
                    replyToVO = buildAuthorVO(parentComment.getUserId());
                }
            }
            return CommentVO.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .user(userVO)
                    .replyTo(replyToVO)
                    .articleId(comment.getArticleId())
                    .isRead(comment.getIsRead() != null && comment.getIsRead() == 1)
                    .status(comment.getStatus())
                    .createdAt(comment.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return new PageResult<>(vos, result.getTotal(), page, pageSize);
    }

    @Override
    public CommentVO createComment(Long userId, Long articleId, CommentCreateRequest request) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setStatus("APPROVED");
        comment.setIsRead(0);
        comment.setCreatedAt(LocalDateTime.now());

        if (request.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            }
        }

        commentMapper.insert(comment);

        return CommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
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

        commentMapper.deleteById(commentId);
    }

    @Override
    public PageResult<CommentVO> getReplies(Long userId, Integer page, Integer pageSize, boolean unreadOnly) {
        QueryWrapper<Comment> userCommentsWrapper = new QueryWrapper<>();
        userCommentsWrapper.eq("user_id", userId);
        List<Comment> userComments = commentMapper.selectList(userCommentsWrapper);

        if (userComments.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0, page, pageSize);
        }

        List<Long> userCommentIds = userComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        QueryWrapper<Comment> replyWrapper = new QueryWrapper<>();
        replyWrapper.in("parent_id", userCommentIds);
        if (unreadOnly) {
            replyWrapper.eq("is_read", 0);
        }
        replyWrapper.orderByDesc("created_at");

        Page<Comment> replyPage = new Page<>(page, pageSize);
        Page<Comment> result = commentMapper.selectPage(replyPage, replyWrapper);

        List<CommentVO> vos = result.getRecords().stream().map(reply -> {
            AuthorVO userVO = buildAuthorVO(reply.getUserId());
            String articleTitle = null;
            Article article = articleMapper.selectById(reply.getArticleId());
            if (article != null) {
                articleTitle = article.getTitle();
            }
            return CommentVO.builder()
                    .id(reply.getId())
                    .content(reply.getContent())
                    .user(userVO)
                    .articleId(reply.getArticleId())
                    .articleTitle(articleTitle)
                    .isRead(reply.getIsRead() != null && reply.getIsRead() == 1)
                    .status(reply.getStatus())
                    .createdAt(reply.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return new PageResult<>(vos, result.getTotal(), page, pageSize);
    }

    @Override
    public PageResult<CommentVO> getAllComments(Integer page, Integer pageSize, String keyword, Long articleId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("content", keyword);
        }
        if (articleId != null) {
            wrapper.eq("article_id", articleId);
        }
        wrapper.orderByDesc("created_at");

        Page<Comment> commentPage = new Page<>(page, pageSize);
        Page<Comment> result = commentMapper.selectPage(commentPage, wrapper);

        List<CommentVO> vos = result.getRecords().stream().map(comment -> {
            AuthorVO userVO = buildAuthorVO(comment.getUserId());
            String articleTitle = null;
            Article article = articleMapper.selectById(comment.getArticleId());
            if (article != null) {
                articleTitle = article.getTitle();
            }
            return CommentVO.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .user(userVO)
                    .articleId(comment.getArticleId())
                    .articleTitle(articleTitle)
                    .isRead(comment.getIsRead() != null && comment.getIsRead() == 1)
                    .status(comment.getStatus())
                    .createdAt(comment.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return new PageResult<>(vos, result.getTotal(), page, pageSize);
    }

    @Override
    public void forceDeleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        commentMapper.deleteById(commentId);
    }

    private AuthorVO buildAuthorVO(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return AuthorVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .build();
    }
}
