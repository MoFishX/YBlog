package com.yvmoux.blog.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Bean
    public StpLogic stpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
                    // 公开接口（不需要登录）
                    SaRouter.match("/auth/**").stop();
                    SaRouter.match("/swagger-ui/**", "/swagger-ui.html").stop();
                    SaRouter.match("/v3/api-docs/**").stop();
                    SaRouter.match("/webjars/**").stop();

                    // 只读内容公开
                    SaRouter.match("/articles", "/articles/*", "/articles/hot").stop();
                    SaRouter.match("/articles/*/comments").stop();
                    SaRouter.match("/user/*").stop();
                    SaRouter.match("/tags").stop();

                    // 管理后台需要管理员角色
                    SaRouter.match("/admin/**", r -> StpUtil.checkRole("ADMIN"));

                    // 其余需要登录
                    SaRouter.match("/**", r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }
}
