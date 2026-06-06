package com.yvmoux.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
//
//    @Bean
//    public StpLogic stpLogicJwt() {
//        return new StpLogicJwtForSimple();
//    }
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new SaInterceptor(handler -> {
//             // 管理后台需要管理员角色
//            SaRouter.match("/admin/**", r -> StpKit.checkRole("ADMIN"));
//            // 其余需要登录
//            SaRouter.match("/**", r -> StpKit.checkLogin());
//        })).addPathPatterns("/**").excludePathPatterns(
//                // 认证
//                "/auth/**",
//                // swagger
//                "/swagger-ui/**",
//                "/swagger-ui.html",
//                "/v3/api-docs/**",
//                "/webjars/**",
//                // 只读内容
//                "/articles",
//                "/articles/*",
//                "/articles/hot",
//                "/articles/*/comments",
//                "/articles/*/view",
//                "/user/*",
//                "/tags"
//        );
//    }
}