package com.yvmoux.blog.aspect;

import com.yvmoux.blog.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@AllArgsConstructor
public class RoleCheckAspect {

    private final UserMapper userMapper;

    /**
     * 角色权限检查切入点
     */
    @Pointcut("execution(* com.yvmoux.blog.service.*.*(..)) && @annotation(com.yvmoux.blog.anno.RoleCheck)")
    public void roleCheckPointcut() {}

//    @Before("roleCheckPointcut()")
//    public void roleCheck() {
//        log.info("角色权限检查");
//        userMapper.selectById()
//
//    }
}
