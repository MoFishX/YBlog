package com.yvmoux.blog;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BlogServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestInfraConfig {

        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
            return Mockito.mock(RedisConnectionFactory.class);
        }

        @Bean
        @Primary
        public ElasticsearchClient elasticsearchClient() {
            return Mockito.mock(ElasticsearchClient.class);
        }
    }
}
