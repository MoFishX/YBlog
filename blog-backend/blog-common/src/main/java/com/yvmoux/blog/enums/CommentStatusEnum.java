package com.yvmoux.blog.enums;

public enum CommentStatusEnum {
    ACTIVE,
    HIDDEN;

    public static boolean contains(String value) {
        for (CommentStatusEnum status : values()) {
            if (status.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}