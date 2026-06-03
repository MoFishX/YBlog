package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.dto.response.LoginResponse;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.enums.RoleEnum;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.UserMapper;
import com.yvmoux.blog.security.JwtTokenProvider;
import com.yvmoux.blog.service.AsyncTaskService;
import com.yvmoux.blog.service.AuthService;
import com.yvmoux.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AsyncTaskService asyncTaskService;
    private final RedisUtils redisUtils;

    @Override
    public void register(RegisterRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(RoleEnum.USER.name());
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        asyncTaskService.sendWelcomeEmail(user.getEmail());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        if ("BANNED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());

        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .articleCount(user.getArticleCount())
                .createdAt(user.getCreatedAt())
                .build();

        return LoginResponse.builder()
                .token(token)
                .expiresIn(86400000L)
                .user(userVO)
                .build();
    }

    @Override
    public LoginResponse refreshToken(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .expiresIn(86400000L)
                .build();
    }

    @Override
    public void logout(String token) {
        Date expiration = jwtTokenProvider.getExpiration(token);
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        if (remainingTime > 0) {
            redisUtils.set("blacklist:token:" + token, "1", remainingTime, TimeUnit.MILLISECONDS);
        }
    }
}
