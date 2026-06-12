package com.yvmoux.blog.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner, EnvironmentAware {

    private final UserMapper userMapper;
    private Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String @NonNull ... args) {
        String username = environment.getProperty("ADMIN_USERNAME");
        String password = environment.getProperty("ADMIN_PASSWORD");
        String email = environment.getProperty("ADMIN_EMAIL");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.info("未设置 ADMIN_USERNAME / ADMIN_PASSWORD，跳过管理员初始化");
            return;
        }

        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (existing != null) {
            log.info("管理员 {} 已存在，跳过创建", username);
            return;
        }

        User admin = User.builder()
                .username(username)
                .password(BCrypt.hashpw(password))
                .email(email)
                .role("ADMIN")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(admin);
        log.info("管理员账号创建成功: {}", username);
    }
}
