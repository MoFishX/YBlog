package com.yvmoux.blog.service;

import org.springframework.scheduling.annotation.Async;

public interface AsyncTaskService {
    @Async
    void sendWelcomeEmail(String email);

    /**
     * 生成文章摘要
     */
    @Async
    void generateArticleSummary(Long articleId);

    /**
     * 生成文章总结
     */
    @Async
    void generateArticleSummaryLong(Long articleId);
}
