package com.yvmoux.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("请先调用 /auth/login 获取 Token，填到这里（不需要加 Bearer 前缀）");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("博客平台 API")
                        .version("1.0.0")
                        .description("基于 Spring Boot 4 + MyBatis-Plus 的博客后端接口文档")
                        .contact(new Contact().name("Blog Team")))
                .addSecurityItem(securityRequirement)
                .schemaRequirement("Authorization", securityScheme);
    }
}
