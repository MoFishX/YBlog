package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yvmoux.blog.converter.ArticleConverter;
import com.yvmoux.blog.converter.TagConverter;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.request.ArticleCreateRequest;
import com.yvmoux.blog.dto.request.ArticleUpdateRequest;
import com.yvmoux.blog.dto.response.AiSummaryVO;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.*;
import com.yvmoux.blog.enums.ArticleStatusEnum;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.*;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.ArticleService;
import com.yvmoux.blog.service.AsyncTaskService;
import com.yvmoux.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private final SecurityUtils securityUtils;
    private final ArticleConverter articleConverter;
    private final TagConverter tagConverter;
    private final AsyncTaskService asyncTaskService;

    @Override
    public PageResult<ArticleVO> getArticleList(Integer page, Integer pageSize, String tagName, String orderBy, String status, Long authorId) {
        // 构建查询条件
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (authorId != null) {
            wrapper.eq("author_id", authorId);
        }
        if (tagName != null) {
            wrapper.exists("select 1 from article_tag at " +
                    "join tag t on at.tag_id = t.id " +
                    "where at.article_id = article.id and t.name = {0}", tagName);
        }
        if (orderBy.equals("hot")) {
            wrapper.orderByDesc("view_count");
        } else {
            wrapper.orderByDesc("created_at");
        }

        return pageResult(page, pageSize, wrapper);
    }

    @Override
    public ArticleVO getArticleDetail(Long articleId, Long currentUserId) {
        // 获取文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 禁止非管理员访问草稿文章
        boolean isAuthor = currentUserId != null && currentUserId.equals(article.getAuthorId());
        boolean isAdmin = currentUserId != null && securityUtils.isAdmin();
        if (!isAuthor && !isAdmin) {
            if (ArticleStatusEnum.DRAFT.name().equals(article.getStatus())) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
        }

        // 增加浏览数并将文章在ZSet中的score加1
        redisUtils.increment("article:view:" + articleId);
        redisUtils.opsForZSet().incrementScore("hot:articles", articleId.toString(), 1);

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 每日站点 PV （Page View）：每次访问都算一次
        redisUtils.increment("stats:views:" + today);
        redisUtils.expire("stats:views:" + today, 8, TimeUnit.DAYS);

        // 每日站点 UV（Unique Visitor）：按登录用户去重
        if (currentUserId != null) {
            redisUtils.opsForSet().add("stats:visitors:" + today, currentUserId.toString());
            redisUtils.expire("stats:visitors:" + today, 8, TimeUnit.DAYS);
        }

        // 查询文章标签
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tagConverter.toTagVOList(tags);

        // 构建返回值
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
                .summary(summary)
                .coverImage(request.getCoverImage())
                .authorId(userId)
                .status(status)
                .viewCount(0L)
                .likeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                    .map(tagId -> ArticleTag.builder()
                            .articleId(article.getId())
                            .tagId(tagId)
                            .build())
                    .toList();
            articleTagMapper.insertBatch(tags);
        }

        // 构建返回值
        User author = userMapper.selectById(userId);
        List<Tag> tags = tagMapper.selectByArticleId(article.getId());
        List<TagVO> tagVOs = tagConverter.toTagVOList(tags);

        // 如果发布状态，异步生成 AI 总结
        if (ArticleStatusEnum.PUBLISHED.name().equals(article.getStatus())) {
            asyncTaskService.generateArticleSummary(article.getId());
        }

        return articleConverter.toArticleVO(article, author, tagVOs, 0, request.getContent());
    }

    @Override
    @Transactional
    public ArticleVO updateArticle(Long articleId, Long userId, ArticleUpdateRequest request) {
        // 获取文章并校验权限
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        if (!article.getAuthorId().equals(userId) && !securityUtils.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 更新文章字段
        if (request.getTitle() != null) article.setTitle(request.getTitle());
        if (request.getSummary() != null) article.setSummary(request.getSummary());
        if (request.getCoverImage() != null) article.setCoverImage(request.getCoverImage());
        if (request.getStatus() != null) article.setStatus(request.getStatus());
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.updateById(article);

        // 更新文章内容
        if (request.getContent() != null) {
            ArticleContent exist = articleContentMapper.selectById(articleId);
            ArticleContent articleContent = ArticleContent.builder()
                    .articleId(articleId)
                    .content(request.getContent())
                    .build();
            if (exist != null) {
                articleContentMapper.updateById(articleContent);
            } else {
                articleContentMapper.insert(articleContent);
            }
        }

        // 更新文章标签
        if (request.getTagIds() != null) {
            articleTagMapper.deleteByArticleId(articleId);
            List<ArticleTag> tags = request.getTagIds().stream()
                    .map(tagId -> ArticleTag.builder()
                            .articleId(articleId)
                            .tagId(tagId)
                            .build())
                    .toList();
            articleTagMapper.insertBatch(tags);
        }

        // 构建返回值
        User author = userMapper.selectById(article.getAuthorId());
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tagConverter.toTagVOList(tags);
        int commentCount = commentMapper.countByArticleId(articleId);

        // 如果更新后为发布状态，异步重新生成 AI 总结
        if (ArticleStatusEnum.PUBLISHED.name().equals(article.getStatus())) {
            asyncTaskService.generateArticleSummary(articleId);
        }

        return articleConverter.toArticleVO(article, author, tagVOs, commentCount, request.getContent());
    }

    @Override
    @Transactional
    public void deleteArticle(String ids, Long userId, boolean isAdmin) {
        List<Long> articleIds;
        try {
            articleIds = Arrays.stream(ids.split(","))
                    .map(Long::valueOf)
                    .toList();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 获取文章
        List<Article> articleList = articleMapper.selectBatchIds(articleIds);
        if (articleList.size() != articleIds.size()) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 先校验全部文章的权限
        for (Article article : articleList) {
            if (!article.getAuthorId().equals(userId) && !isAdmin) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        // 删除文章、标签、评论、喜欢、文章内容
        for (Long articleId : articleIds) {
            articleTagMapper.deleteByArticleId(articleId);
            userLikeMapper.delete(new LambdaQueryWrapper<UserLike>().eq(UserLike::getArticleId, articleId));
            commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, articleId));
            articleContentMapper.deleteById(articleId);
            articleMapper.deleteById(articleId);
        }
    }

    @Override
    @Transactional
    public void deleteArticleOne(Long articleId, Long userId) {
        deleteArticle(articleId.toString(), userId, false);
    }


    @Override
    @Transactional
    public ArticleVO toggleLike(Long articleId, Long userId) {
        // 获取文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 判断是否已点赞，执行点赞或取消点赞
        boolean liked = userLikeMapper.countByUserAndArticle(userId, articleId) > 0;
        if (liked) {
            LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserLike::getUserId, userId).eq(UserLike::getArticleId, articleId);
            userLikeMapper.delete(wrapper);
            articleMapper.incrementLikeCount(articleId, -1L);
            redisUtils.opsForZSet().incrementScore("hot:articles", articleId.toString(), -2);
        } else {
            UserLike userLike = UserLike.builder()
                    .userId(userId)
                    .articleId(articleId)
                    .createdAt(LocalDateTime.now())
                    .build();
            userLikeMapper.insert(userLike);
            articleMapper.incrementLikeCount(articleId, 1L);
            redisUtils.opsForZSet().incrementScore("hot:articles", articleId.toString(), 2);
        }

        // 构建返回值
        article = articleMapper.selectById(articleId);
        User author = userMapper.selectById(article.getAuthorId());
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        List<TagVO> tagVOs = tagConverter.toTagVOList(tags);
        int commentCount = commentMapper.countByArticleId(articleId);
        ArticleVO vo = articleConverter.toArticleVO(article, author, tagVOs, commentCount, null);
        vo.setIsLiked(!liked);

        return vo;
    }

    @Override
    public List<ArticleVO> getHotArticles(int limit) {
        // 从 ZSet 获取热门文章 ID
        Set<Object> members = redisUtils.opsForZSet().reverseRange("hot:articles", 0, limit - 1);
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建返回值
        List<ArticleVO> records = new ArrayList<>();
        for (Object member : members) {
            Long articleId = Long.parseLong(member.toString());
            Article article = articleMapper.selectById(articleId);
            if (article == null) continue;

            User author = userMapper.selectById(article.getAuthorId());
            List<Tag> tags = tagMapper.selectByArticleId(articleId);
            List<TagVO> tagVOs = tagConverter.toTagVOList(tags);
            int commentCount = commentMapper.countByArticleId(articleId);
            records.add(articleConverter.toArticleVO(article, author, tagVOs, commentCount, null));
        }

        Collections.reverse(records);

        return records;
    }

    @Override
    public PageResult<ArticleVO> getAllArticles(Integer page, Integer pageSize, String status, String keyword) {
        // 构建查询条件
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("title", keyword);
        }
        wrapper.orderByDesc("created_at");

        return pageResult(page, pageSize, wrapper);
    }

    @Override
    public PageResult<ArticleVO> search(String keyword, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        long total = articleMapper.countSearch(keyword);
        List<Article> articles = articleMapper.searchByKeyword(keyword, offset, pageSize);

        List<ArticleVO> records = new ArrayList<>();
        for (Article article : articles) {
            ArticleVO vo = articleConverter.toArticleVO(
                    article,
                    userMapper.selectById(article.getAuthorId()),
                    tagConverter.toTagVOList(tagMapper.selectByArticleId(article.getId())),
                    commentMapper.countByArticleId(article.getId()),
                    null
            );
            records.add(vo);
        }
        return new PageResult<>(records, total);
    }

    @Override
    public void triggerAiSummary(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        asyncTaskService.generateArticleSummary(articleId);
    }

    @Override
    public void deleteAiSummary(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        article.setAiSummary(null);
        articleMapper.updateById(article);
    }

    @Override
    public AiSummaryVO getAiSummary(Long articleId) {
        Article article = articleMapper.selectById(articleId);

        String aiSummary = article.getAiSummary();
        int status = 1;

        if (aiSummary == null || aiSummary.isBlank()) {
            status = 0;
        }

        return AiSummaryVO.builder()
                .status(status)
                .summary(aiSummary)
                .build();
    }

    private PageResult<ArticleVO> pageResult(Integer page, Integer pageSize, QueryWrapper<Article> wrapper) {
        // 分页查询
        Page<Article> articlePage = articleMapper.selectPage(new Page<>(page, pageSize), wrapper);

        // 构建返回值
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

    private String extractPlainText(String markdown) {
        if (markdown == null) return "";

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

        return text.length() > 200 ? text.substring(0, 200) : text;
    }
}
