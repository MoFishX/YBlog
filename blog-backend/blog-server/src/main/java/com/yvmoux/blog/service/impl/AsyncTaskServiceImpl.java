package com.yvmoux.blog.service.impl;

import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.entity.ArticleContent;
import com.yvmoux.blog.mapper.ArticleContentMapper;
import com.yvmoux.blog.mapper.ArticleMapper;
import com.yvmoux.blog.service.AIService;
import com.yvmoux.blog.service.AsyncTaskService;
import com.yvmoux.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final ArticleMapper articleMapper;
    private final ArticleContentMapper articleContentMapper;
    private final AIService aiService;
    private final RedisUtils redisUtils;

    @Async
    @Override
    public void sendWelcomeEmail(String email) {
        // TODO  发送邮件
        log.info("收到邮件任务: {}", email);
    }

    @Async
    @Override
    public void generateArticleSummary(Long articleId) {
        // Redis 防重
        String lockKey = "ai:summary:lock:" + articleId;
        if (Boolean.TRUE.equals(redisUtils.hasKey(lockKey))) {{
            log.info("文章 {} 的 AI 总结任务已被锁定，跳过", articleId);
            return;
        }}
        redisUtils.set(lockKey, "1", 10, TimeUnit.MINUTES);

        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                log.warn("文章 {} 不存在，跳过 AI 总结", articleId);
                return;
            }

            ArticleContent content = articleContentMapper.selectById(articleId);
            if (content == null || content.getContent() == null || content.getContent().isBlank()) {
                log.warn("文章 {} 内容为空，跳过 AI 总结", articleId);
                return;
            }

            String summary = aiService.summarize(article.getTitle(), content.getContent());

            if (summary != null && !summary.isBlank()) {
                article.setAiSummary(summary);
                articleMapper.updateById(article);
                log.info("文章 {} AI 总结写入成功", articleId);
            }
        } catch (Exception e) {
            log.error("文章 {} AI 总结生成失败", articleId, e);
        } finally {
            redisUtils.delete(lockKey);
        }
    }
}
