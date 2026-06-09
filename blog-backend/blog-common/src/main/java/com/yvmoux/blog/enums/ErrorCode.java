package com.yvmoux.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(200, "ok"),

    FILE_TOO_LARGE(400, "文件大小超出限制"),
    FILE_TYPE_INVALID(400, "文件类型不支持"),
    BAD_REQUEST(400, "参数错误"),
    WRONG_PASSWORD(400, "密码错误"),

    WRONG_USER_OR_PASSWORD(401, "用户名或密码错误"),
    UNAUTHORIZED(401, "未认证或Token已过期"),
    REFRESH_TOKEN_BLACKLISTED(401, "RefreshToken已失效，请重新登录"),

    FORBIDDEN(403, "无权限执行此操作"),
    USER_BANNED(403, "用户已被封禁"),

    ARTICLE_NOT_FOUND(404, "文章不存在"),
    COMMENT_NOT_FOUND(404, "评论不存在"),
    USER_NOT_FOUND(404, "用户不存在"),
    NOT_FOUND(404, "资源不存在"),

    USERNAME_EXISTS(409, "用户名已存在"),
    TAG_NAME_EXISTS(409, "标签名已存在"),
    CONFLICT(409, "资源冲突"),

    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    INTERNAL_ERROR(500, "服务器内部错误");


    private final int code;
    private final String message;
}
