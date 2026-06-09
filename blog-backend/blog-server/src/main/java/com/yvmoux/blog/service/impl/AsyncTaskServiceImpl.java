package com.yvmoux.blog.service.impl;

import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.mapper.ArticleMapper;
import com.yvmoux.blog.service.AIService;
import com.yvmoux.blog.service.AsyncTaskService;
import com.yvmoux.blog.utils.MailUtil;
import com.yvmoux.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final ArticleMapper articleMapper;
    private final AIService aiService;
    private final RedisUtils redisUtils;
    private final MailUtil mailUtil;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Async
    @Override
    public void generateArticleSummary(Long articleId, String title, String content) {
        generateArticleAi(articleId, title, content, ArticleAiType.SUMMARY);
    }

    @Async
    @Override
    public void generateArticleSummaryLong(Long articleId, String title, String content) {
        generateArticleAi(articleId, title, content, ArticleAiType.SUMMARY_LONG);
    }

    private void generateArticleAi(Long articleId, String title, String content, ArticleAiType type) {
        String lockKey = "ai:generate:lock:" + type.name() + ":" + articleId;
        if (Boolean.TRUE.equals(redisUtils.hasKey(lockKey))) {
            log.warn("文章 {} 的 AI {} 任务已被锁定，跳过", articleId, type.name());
            return;
        }
        redisUtils.set(lockKey, "1", 10, TimeUnit.MINUTES);

        try {
            if (content == null || content.isBlank()) {
                log.warn("文章 {} 内容为空，跳过 AI 生成", articleId);
                return;
            }

            boolean success = switch (type) {
                case SUMMARY -> {
                    String result = aiService.summarize(title, content);
                    if (result != null && !result.isBlank()) {
                        Article article = articleMapper.selectById(articleId);
                        if (article == null) {
                            log.warn("文章 {} 已被删除，跳过 AI 写入", articleId);
                            yield false;
                        }
                        article.setAiSummary(result);
                        articleMapper.updateById(article);
                        yield true;
                    }
                    yield false;
                }
                case SUMMARY_LONG -> {
                    String result = aiService.summarizeLong(title, content);
                    if (result != null && !result.isBlank()) {
                        Article article = articleMapper.selectById(articleId);
                        if (article == null) {
                            log.warn("文章 {} 已被删除，跳过 AI 写入", articleId);
                            yield false;
                        }
                        article.setAiSummaryLong(result);
                        articleMapper.updateById(article);
                        yield true;
                    }
                    yield false;
                }
            };
            if (success) {
                log.info("文章 {} AI {} 生成成功", articleId, type.name());
            }
        } catch (Exception e) {
            log.error("文章 {} AI {} 生成失败", articleId, type.name(), e);
        } finally {
            redisUtils.delete(lockKey);
        }
    }

    @Async
    @Override
    public void sendActivationEmail(Long userId, String email) {
        String token = UUID.randomUUID().toString();
        redisUtils.set("email:verify:" + token, userId, 24, TimeUnit.HOURS);

        String verifyUrl = frontendUrl + "/verify-email?token=" + token;
        mailUtil.sendHtml("Blog <noreply@mail.yvmoux.com>", email, "激活你的博客账号",
                "<p>点击以下链接激活你的博客账号：</p>" +
                "<p><a href=\"" + verifyUrl + "\">" + verifyUrl + "</a></p>" +
                "<p>链接 24 小时内有效。</p>");

        log.info("激活邮件已发送至 {}, userId: {}", email, userId);
    }

    enum ArticleAiType {
        SUMMARY,
        SUMMARY_LONG
    }
}
