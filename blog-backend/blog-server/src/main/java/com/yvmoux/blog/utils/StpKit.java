package com.yvmoux.blog.utils;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;

public class StpKit {

    public static void login(Long userId, String role) {
        StpUtil.login(userId);
        StpUtil.getSession().set("role", role);
    }

    public static void logout() {
        StpUtil.logout();
    }

    public static Long getLoginId() {
        return StpUtil.getLoginIdAsLong();
    }

    public static boolean isAdmin() {
        return StpUtil.hasRole("ADMIN");
    }

    public static void checkLogin() {
        StpUtil.checkLogin();
    }

    public static void checkRole(String role) {
        StpUtil.checkRole(role);
    }

    public static SaTokenInfo getTokenInfo() {
        return StpUtil.getTokenInfo();
    }
}
