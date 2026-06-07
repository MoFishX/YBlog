package com.yvmoux.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AIConfig {
    private String baseUrl;
    private String apiKey;
    private String model;
    private int maxTokens = 1024;
    private double temperature = 0.3;
    private boolean enabled = true;
}
