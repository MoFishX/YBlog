package com.yvmoux.blog.constant;

public class JwtConstants {
    /**
     * HTTP 请求头名称（可自定义）
     * 请求头格式：
     * 【名字】: 【内容】
     * 标准格式：
     * Authorization: Bearer xxxx
     */
    public static final String AUTH_HEADER = "Authorization";

    // 请求头前缀（固定：大写B + 空格）
    public static final String BEARER_PREFIX = "Bearer ";

    // Swagger 认证 scheme（固定小写）
    public static final String SCHEME_BEARER = "bearer";

    // Swagger 安全方案名称（可自定义）
    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    // 说明文字（可自定义）
    public static final String BEARER_FORMAT = "JWT";
}
