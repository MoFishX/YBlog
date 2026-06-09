package com.yvmoux.blog.service;

import org.springframework.scheduling.annotation.Async;

public interface AsyncTaskService {
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

    /**
     * 发送激活邮件
     */
    @Async
    void sendActivationEmail(Long userId, String email);
}
