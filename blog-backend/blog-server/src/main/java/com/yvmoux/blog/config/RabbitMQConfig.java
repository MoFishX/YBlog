package com.yvmoux.blog.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 文章同步到ES的消息总线
    public static final String ARTICLE_EXCHANGE = "article.exchange";
    public static final String ARTICLE_SYNC_QUEUE = "article.sync.queue";
    public static final String ARTICLE_SYNC_KEY = "article.sync";

    // 注册邮件通知的消息总线
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_KEY = "email.send";

    @Bean
    public Queue articleSyncQueue() {
        return QueueBuilder.durable(ARTICLE_SYNC_QUEUE).build();
    }

    @Bean
    public DirectExchange articleExchange() {
        return new DirectExchange(ARTICLE_EXCHANGE);
    }

    @Bean
    public Binding articleSyncBinding() {
        return BindingBuilder.bind(articleSyncQueue()).to(articleExchange()).with(ARTICLE_SYNC_KEY);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(emailExchange()).with(EMAIL_KEY);
    }
}
