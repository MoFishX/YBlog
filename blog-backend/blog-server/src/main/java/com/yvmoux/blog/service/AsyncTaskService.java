package com.yvmoux.blog.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskService {

    private final ArticleMapper articleMapper;
    private final ElasticsearchClient esClient;

    @Async
    public void syncArticleToES(Long articleId) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                try {
                    esClient.delete(d -> d.index("articles").id(String.valueOf(articleId)));
                } catch (Exception ignored) {}
                return;
            }

            Map<String, Object> doc = Map.of(
                "id", article.getId(),
                "title", article.getTitle(),
                "content", article.getContent() != null ? article.getContent() : "",
                "summary", article.getSummary() != null ? article.getSummary() : "",
                "authorId", article.getAuthorId(),
                "status", article.getStatus() != null ? article.getStatus() : "PUBLISHED",
                "createdAt", article.getCreatedAt() != null ? article.getCreatedAt().toString() : ""
            );

            esClient.index(i -> i.index("articles").id(String.valueOf(articleId)).document(doc));
            log.info("文章 {} 同步到 ES 完成", articleId);
        } catch (Exception e) {
            log.error("文章 {} 同步到 ES 失败", articleId, e);
        }
    }

    @Async
    public void sendWelcomeEmail(String email) {
        log.info("收到邮件任务: {}", email);
    }
}
