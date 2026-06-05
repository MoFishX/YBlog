package com.yvmoux.blog.enums;

public enum ArticleStatusEnum {
    DRAFT,
    PUBLISHED,
    REVIEWING;

    public static boolean contains(String value) {
        for (ArticleStatusEnum status : values()) {
            if (status.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
