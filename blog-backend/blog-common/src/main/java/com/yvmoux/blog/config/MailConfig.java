package com.yvmoux.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email.resend")
@Data
public class MailConfig {
    private String apiKey;
}
