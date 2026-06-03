package com.yvmoux.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncTaskService {

    @Async
    public void sendWelcomeEmail(String email) {
        log.info("收到邮件任务: {}", email);
    }
}
