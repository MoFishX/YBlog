package com.yvmoux.blog.mq.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleSyncProducer {

    private final RabbitTemplate rabbitTemplate;

    public void syncToES(Long articleId) {
        rabbitTemplate.convertAndSend("article.exchange", "article.sync", articleId);
    }
}
