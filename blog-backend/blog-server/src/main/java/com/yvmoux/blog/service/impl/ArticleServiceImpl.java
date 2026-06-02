package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yvmoux.blog.config.RabbitMQConfig;
import com.yvmoux.blog.converter.ArticleConverter;
import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.AuthorVO;
import com.yvmoux.blog.dto.response.PageResult;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.entity.ArticleTag;
import com.yvmoux.blog.entity.Tag;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.entity.UserLike;
import com.yvmoux.blog.enums.ArticleStatusEnum;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.ArticleMapper;
import com.yvmoux.blog.mapper.ArticleTagMapper;
import com.yvmoux.blog.mapper.CommentMapper;
import com.yvmoux.blog.mapper.TagMapper;
import com.yvmoux.blog.mapper.UserLikeMapper;
import com.yvmoux.blog.mapper.UserMapper;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.ArticleService;
import com.yvmoux.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserLikeMapper userLikeMapper;
    private final SecurityUtils securityUtils;
    private final RedisUtils redisUtils;
    private final RabbitTemplate rabbitTemplate;
    private final ArticleConverter articleConverter;

    @Override
    public PageResult<ArticleVO> getArticleList(Integer page, Integer pageSize, Long tagId, String orderBy, String status, Long userId) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.PUBLISHED.name());

        if (tagId != null) {
            wrapper.exists("SELECT 1 FROM article_tag WHERE article_tag.article_id = article.id AND article_tag.tag_id = {0}", tagId);
        }

        if ("hot".equals(orderBy)) {
            wrapper.orderByDesc("view_count");
        } else if ("oldest".equals(orderBy)) {
            wrapper.orderByAsc("created_at");
        } else {
            wrapper.orderByDesc("created_at");
        }

        Page<Article> articlePage = new Page<>(page, pageSize);
        articlePage = articleMapper.selectPage(articlePage, wrapper);

        List<ArticleVO> records = new ArrayList<>();
        for (Article article : articlePage.getRecords()) {
            List<Tag> tags = tagMapper.selectByArticleId(article.getId());
            List<TagVO> tagVOs = tags.stream()
                    .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                    .collect(Collectors.toList());
            User author = userMapper.selectById(article.getAuthorId());
            int commentCount = commentMapper.countByArticleId(article.getId());
            ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount);
            records.add(vo);
        }

        return new PageResult<>(records, articlePage.getTotal(), page, pageSize);
    }

    @Override
    public ArticleVO getArticleDetail(Long articleId, Long currentUserId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        boolean isAuthor = currentUserId != null && currentUserId.equals(article.getAuthorId());
        boolean isAdmin = currentUserId != null && securityUtils.isAdmin();

        if (currentUserId != null && !isAuthor && !isAdmin) {
            if (ArticleStatusEnum.DRAFT.name().equals(article.getStatus())) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
        }

        redisUtils.increment("article:view:" + articleId);
        redisUtils.opsForZSet().incrementScore("hot:articles", articleId.toString(), 1);

        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tags.stream()
                .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                .collect(Collectors.toList());
        User author = userMapper.selectById(article.getAuthorId());
        int commentCount = commentMapper.countByArticleId(articleId);
        ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount);

        if (currentUserId != null) {
            boolean liked = userLikeMapper.countByUserAndArticle(currentUserId, articleId) > 0;
            vo.setIsLiked(liked);
        }

        return vo;
    }

    @Override
    @Transactional
    public ArticleVO createArticle(Long userId, ArticleCreateRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setAuthorId(userId);
        article.setStatus(request.getStatus() != null ? request.getStatus() : ArticleStatusEnum.DRAFT.name());

        if (request.getSummary() != null && !request.getSummary().isBlank()) {
            article.setSummary(request.getSummary());
        } else {
            article.setSummary(extractPlainText(request.getContent()));
        }

        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());

        articleMapper.insert(article);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.ARTICLE_EXCHANGE, RabbitMQConfig.ARTICLE_SYNC_KEY, article.getId());

        User author = userMapper.selectById(userId);
        List<Tag> tags = tagMapper.selectByArticleId(article.getId());
        List<TagVO> tagVOs = tags.stream()
                .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                .collect(Collectors.toList());

        return articleConverter.toArticleVO(article, author, tagVOs, 0);
    }

    @Override
    @Transactional
    public ArticleVO updateArticle(Long articleId, Long userId, ArticleUpdateRequest request) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        boolean isAdmin = securityUtils.isAdmin();
        if (!article.getAuthorId().equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }
        if (request.getCoverImage() != null) {
            article.setCoverImage(request.getCoverImage());
        }
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.updateById(article);

        if (request.getTagIds() != null) {
            articleTagMapper.deleteByArticleId(articleId);
            for (Long tagId : request.getTagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.ARTICLE_EXCHANGE, RabbitMQConfig.ARTICLE_SYNC_KEY, article.getId());

        User author = userMapper.selectById(article.getAuthorId());
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tags.stream()
                .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                .collect(Collectors.toList());
        int commentCount = commentMapper.countByArticleId(articleId);

        return articleConverter.toArticleVO(article, author, tagVOs, commentCount);
    }

    @Override
    public void deleteArticle(Long articleId, Long userId, boolean isAdmin) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        if (!article.getAuthorId().equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        articleMapper.deleteById(articleId);
    }

    @Override
    @Transactional
    public ArticleVO toggleLike(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        boolean liked = userLikeMapper.countByUserAndArticle(userId, articleId) > 0;

        if (liked) {
            LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserLike::getUserId, userId).eq(UserLike::getArticleId, articleId);
            userLikeMapper.delete(wrapper);
            articleMapper.incrementLikeCount(articleId, -1L);
            redisUtils.opsForZSet().incrementScore("hot:articles", articleId.toString(), -2);
        } else {
            UserLike userLike = new UserLike();
            userLike.setUserId(userId);
            userLike.setArticleId(articleId);
            userLike.setCreatedAt(LocalDateTime.now());
            userLikeMapper.insert(userLike);
            articleMapper.incrementLikeCount(articleId, 1L);
            redisUtils.opsForZSet().incrementScore("hot:articles", articleId.toString(), 2);
        }

        article = articleMapper.selectById(articleId);
        User author = userMapper.selectById(article.getAuthorId());
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tags.stream()
                .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                .collect(Collectors.toList());
        int commentCount = commentMapper.countByArticleId(articleId);
        ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount);
        vo.setIsLiked(!liked);

        return vo;
    }

    @Override
    public PageResult<ArticleVO> getHotArticles(int limit) {
        Set<Object> members = redisUtils.opsForZSet().reverseRange("hot:articles", 0, limit - 1);
        if (members == null || members.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0, 1, limit);
        }

        List<ArticleVO> records = new ArrayList<>();
        for (Object member : members) {
            Long articleId = Long.parseLong(member.toString());
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                continue;
            }
            User author = userMapper.selectById(article.getAuthorId());
            List<Tag> tags = tagMapper.selectByArticleId(articleId);
            List<TagVO> tagVOs = tags.stream()
                    .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                    .collect(Collectors.toList());
            int commentCount = commentMapper.countByArticleId(articleId);
            ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount);
            records.add(vo);
        }

        return new PageResult<>(records, records.size(), 1, limit);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            articleMapper.deleteById(id);
        }
    }

    @Override
    @Transactional
    public ArticleVO reviewArticle(Long articleId, String status, String reason) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        if ("APPROVED".equals(status)) {
            article.setStatus(ArticleStatusEnum.PUBLISHED.name());
        } else if ("REJECTED".equals(status)) {
            article.setStatus(ArticleStatusEnum.DRAFT.name());
        } else {
            article.setStatus(status);
        }

        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.updateById(article);

        rabbitTemplate.convertAndSend(RabbitMQConfig.ARTICLE_EXCHANGE, RabbitMQConfig.ARTICLE_SYNC_KEY, article.getId());

        User author = userMapper.selectById(article.getAuthorId());
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tags.stream()
                .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                .collect(Collectors.toList());
        int commentCount = commentMapper.countByArticleId(articleId);

        return articleConverter.toArticleVO(article, author, tagVOs, commentCount);
    }

    @Override
    public PageResult<ArticleVO> getAllArticles(Integer page, Integer pageSize, String status, String keyword) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("title", keyword);
        }
        wrapper.orderByDesc("created_at");

        Page<Article> articlePage = new Page<>(page, pageSize);
        articlePage = articleMapper.selectPage(articlePage, wrapper);

        List<ArticleVO> records = new ArrayList<>();
        for (Article article : articlePage.getRecords()) {
            List<Tag> tags = tagMapper.selectByArticleId(article.getId());
            List<TagVO> tagVOs = tags.stream()
                    .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                    .collect(Collectors.toList());
            User author = userMapper.selectById(article.getAuthorId());
            int commentCount = commentMapper.countByArticleId(article.getId());
            ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount);
            records.add(vo);
        }

        return new PageResult<>(records, articlePage.getTotal(), page, pageSize);
    }

    private String extractPlainText(String markdown) {
        if (markdown == null) {
            return "";
        }
        String text = markdown
                .replaceAll("#{1,6}\\s*", "")
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("\\*(.+?)\\*", "$1")
                .replaceAll("`{1,3}[^`]*`{1,3}", "")
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                .replaceAll("\\[([^\\]]*)\\]\\(.*?\\)", "$1")
                .replaceAll(">\\s*", "")
                .replaceAll("[-*+]\\s+", "")
                .replaceAll("\\d+\\.\\s+", "")
                .replaceAll("\\|.*?\\|", "")
                .replaceAll("---+", "")
                .replaceAll("\\n+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (text.length() > 200) {
            text = text.substring(0, 200);
        }
        return text;
    }
}
