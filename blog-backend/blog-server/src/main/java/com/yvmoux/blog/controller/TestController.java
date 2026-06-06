package com.yvmoux.blog.controller;
//
//import cn.dev33.satoken.stp.StpUtil;
//import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
//    /**
//     * 测试接口（需要登录才能访问）
//     * @return 测试结果
//     */
//    @GetMapping("/hello")
//    public SaResult hello() {
//        // 获取当前登录用户ID（登录后才能获取，未登录会被拦截）
//        long userId = StpUtil.getLoginIdAsLong();
//
//        return SaResult.ok("访问成功").setData("当前登录用户ID：" + userId);
//    }
@GetMapping("/hello")
public String hello(){
    return "hello";
}

}
