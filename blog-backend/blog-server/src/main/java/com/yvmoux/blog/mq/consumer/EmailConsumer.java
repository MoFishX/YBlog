package com.yvmoux.blog.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailConsumer {

    @RabbitListener(queues = "email.queue")
    public void handleEmail(String message) {
        log.info("收到邮件任务: {}", message);
        // TODO: 接入真实邮件服务（如 JavaMailSender）
    }
}
