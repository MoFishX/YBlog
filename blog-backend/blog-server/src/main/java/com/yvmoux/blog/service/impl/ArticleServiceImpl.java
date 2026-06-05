package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yvmoux.blog.converter.ArticleConverter;
import com.yvmoux.blog.converter.TagConverter;
import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.entity.ArticleContent;
import com.yvmoux.blog.entity.ArticleTag;
import com.yvmoux.blog.entity.Tag;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.entity.UserLike;
import com.yvmoux.blog.enums.ArticleStatusEnum;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.enums.RoleEnum;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.ArticleContentMapper;
import com.yvmoux.blog.mapper.ArticleMapper;
import com.yvmoux.blog.mapper.ArticleTagMapper;
import com.yvmoux.blog.mapper.CommentMapper;
import com.yvmoux.blog.mapper.TagMapper;
import com.yvmoux.blog.mapper.UserLikeMapper;
import com.yvmoux.blog.mapper.UserMapper;
import com.yvmoux.blog.service.ArticleService;
import com.yvmoux.blog.utils.RedisUtils;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleContentMapper articleContentMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserLikeMapper userLikeMapper;
    private final RedisUtils redisUtils;
    private final ArticleConverter articleConverter;
    private final TagConverter tagConverter;

    @Override
    public PageResult<ArticleVO> getArticleList(Integer page, Integer pageSize, String tagName, String orderBy, String status, Long userId) {

        // 条件查询
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        // 筛选状态
        if (status != null) {
            wrapper.eq("status", status);
        }
        // 筛选作者
        if (userId != null) {
            wrapper.eq("author_id", userId);
        }
        // 筛选标签
        if (tagName != null) {
            wrapper.exists("select 1 from article_tag at " +
                    "join tag t on at.tag_id = t.id " +
                    "where at.article_id = article.id and t.name = {0}", tagName);
        }
        // 排序
        if (orderBy.equals("hot")) {
            wrapper.orderByDesc("view_count");
        } else {
            wrapper.orderByDesc("created_at");
        }

        // 分页
        Page<Article> articlePage = new Page<>(page, pageSize);
        articlePage = articleMapper.selectPage(articlePage, wrapper);

        List<ArticleVO> records = new ArrayList<>();

        articlePage.getRecords().forEach(article -> {
            ArticleVO articleVO = articleConverter.toArticleVO(
                    article,
                    userMapper.selectById(article.getAuthorId()),
                    tagConverter.toTagVOList(tagMapper.selectByArticleId(article.getId())),
                    commentMapper.countByArticleId(article.getId()),
                    null
            );
            records.add(articleVO);
        });

        return new PageResult<>(records, articlePage.getTotal());
    }

    @Override
    public ArticleVO getArticleDetail(Long articleId) {
        // 获取对象
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;

        // 禁止非管理员访问草稿文章
        boolean isAuthor = currentUserId != null && currentUserId.equals(article.getAuthorId());
        boolean isAdmin = currentUserId != null && StpUtil.hasRole(RoleEnum.ADMIN.name());

        if (!isAuthor && !isAdmin) {
            if (ArticleStatusEnum.DRAFT.name().equals(article.getStatus())) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
        }

        // 增加浏览数并将文章在ZSet中的score加1
        redisUtils.increment("article:view:" + articleId);
        redisUtils.opsForZSet().incrementScore("hot:articles", articleId.toString(), 1);

        // 查询文章标签
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tagConverter.toTagVOList(tags);

        ArticleVO vo = articleConverter.toArticleVO(
                article,
                userMapper.selectById(article.getAuthorId()),
                tagVOs,
                commentMapper.countByArticleId(articleId),
                articleContentMapper.selectById(articleId).getContent()
        );

        // 判断用户是否点赞
        if (currentUserId != null) {
            boolean liked = userLikeMapper.countByUserAndArticle(currentUserId, articleId) > 0;
            vo.setIsLiked(liked);
        }

        return vo;
    }

    @Override
    @Transactional
    public ArticleVO createArticle(Long userId, ArticleCreateRequest request) {
        // 设置文章状态
        String status;
        if (request.getStatus() == null || !ArticleStatusEnum.contains(request.getStatus().toUpperCase())) {
            status = ArticleStatusEnum.DRAFT.name();
        } else {
            status = request.getStatus().toUpperCase();
        }
        // 获取文章内容
        String content;
        if (request.getContent() == null || request.getContent().isBlank()) {
            content = "似乎什么也没写呢~";
        } else {
            content = request.getContent();
        }
        // 设置文章摘要
        String summary;
        if (request.getSummary() == null || request.getSummary().isBlank()) {
            summary = extractPlainText(request.getContent());
            if (content.equals("似乎什么也没写呢~")) {
                summary = "似乎什么也没写呢~";
            }
        } else {
            summary = request.getSummary();
        }


        // 插入文章
        Article article = Article.builder()
                .id(null)
                .title(request.getTitle())
                .summary(summary) // TODO 实现AI摘要
                .coverImage(request.getCoverImage())
                .authorId(userId)
                .status(status)
                .viewCount(0L)
                .likeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();
        articleMapper.insert(article);

        // 插入文章内容
        ArticleContent articleContent = ArticleContent.builder()
                .articleId(article.getId())
                .content(content)
                .build();
        articleContentMapper.insert(articleContent);

        // 插入文章标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<ArticleTag> tags = request.getTagIds().stream()
                    .map(tagId -> {
                        return ArticleTag.builder()
                                .articleId(article.getId())
                                .tagId(tagId)
                                .build();
                    })
                    .toList();
            articleTagMapper.insert(tags);
        }

        User author = userMapper.selectById(userId);
        List<Tag> tags = tagMapper.selectByArticleId(article.getId());
        List<TagVO> tagVOs = tagConverter.toTagVOList(tags);

        return articleConverter.toArticleVO(article, author, tagVOs, 0, request.getContent());
    }

    @Override
    @Transactional
    public ArticleVO updateArticle(Long articleId, Long userId, ArticleUpdateRequest request) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        boolean isAdmin = StpUtil.hasRole("ADMIN");
        if (!article.getAuthorId().equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
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

        if (request.getContent() != null) {
            ArticleContent articleContent = articleContentMapper.selectById(articleId);
            if (articleContent != null) {
                articleContent.setContent(request.getContent());
                articleContentMapper.updateById(articleContent);
            } else {
                articleContent = new ArticleContent();
                articleContent.setArticleId(articleId);
                articleContent.setContent(request.getContent());
                articleContentMapper.insert(articleContent);
            }
        }

        if (request.getTagIds() != null) {
            articleTagMapper.deleteByArticleId(articleId);
            for (Long tagId : request.getTagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        User author = userMapper.selectById(article.getAuthorId());
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tags.stream()
                .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                .collect(Collectors.toList());
        int commentCount = commentMapper.countByArticleId(articleId);

        return articleConverter.toArticleVO(article, author, tagVOs, commentCount, request.getContent());
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
        articleContentMapper.deleteById(articleId);
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
        ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount, null);
        vo.setIsLiked(!liked);

        return vo;
    }

    @Override
    public List<ArticleVO> getHotArticles(int limit) {
        Set<Object> members = redisUtils.opsForZSet().reverseRange("hot:articles", 0, limit - 1);
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
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
            ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount, null);
            records.add(vo);
        }

        return records;
    }

    @Override
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            articleMapper.deleteById(id);
            articleContentMapper.deleteById(id);
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

        User author = userMapper.selectById(article.getAuthorId());
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tags.stream()
                .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                .collect(Collectors.toList());
        int commentCount = commentMapper.countByArticleId(articleId);

        return articleConverter.toArticleVO(article, author, tagVOs, commentCount, null);
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
            ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount, null);
            records.add(vo);
        }

        return new PageResult<>(records, articlePage.getTotal());
    }

    /**
     * 提取纯文本
     */
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
