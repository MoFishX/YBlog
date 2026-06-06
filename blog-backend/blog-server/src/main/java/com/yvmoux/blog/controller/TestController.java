package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    /**
     * 放行接口（白名单）
     *
     * @return 结果
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("你无需登录！");
    }

    /**
     * 需要登录，但不需要角色（所有登录用户都能访问）
     *
     * @return 结果
     */
    @GetMapping("/auth")
    public Result<String> auth() {
        return Result.success("你已登录！");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Result<String> admin() {
        return Result.success("管理员页面！");
    }
}
