package com.yvmoux.blog.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageConfig {
    /**
     * 存储类型：local / oss
     */
    private String type = "local";

    /**
     * 本地存储配置
     */
    private Local local;

    /**
     * OSS 对象存储配置
     */
    private Oss oss;

    // 本地存储子配置
    @Data
    public static class Local {
        private String path = "uploads"; // 默认值
    }

    // OSS 子配置
    @Data
    public static class Oss {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String pathPrefix = "yblog/uploads";
    }
}
