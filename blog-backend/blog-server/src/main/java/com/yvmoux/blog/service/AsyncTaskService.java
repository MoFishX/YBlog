package com.yvmoux.blog.service;

import org.springframework.scheduling.annotation.Async;

public interface AsyncTaskService {
    @Async
    void sendWelcomeEmail(String email);

    @Async
    void generateArticleSummary(Long articleId);
}
