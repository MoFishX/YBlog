package com.yvmoux.blog.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.dto.LoginResult;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.enums.RoleEnum;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.UserMapper;
import com.yvmoux.blog.service.AsyncTaskService;
import com.yvmoux.blog.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final AsyncTaskService asyncTaskService;

    @Override
    public void register(RegisterRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(RoleEnum.USER.name());
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        asyncTaskService.sendWelcomeEmail(user.getEmail());
    }

    @Override
    public LoginResult login(LoginRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        if ("BANNED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        StpUtil.login(user.getId());
        // 将角色存入 Sa-Token Session，供后续 @SaCheckRole 使用
        StpUtil.getSession().set("role", user.getRole());

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

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

        return LoginResult.builder()
                .token(tokenInfo.getTokenValue())
                .expiresIn(tokenInfo.getTokenTimeout() * 1000L)
                .user(userVO)
                .build();
    }

    @Override
    public LoginResult refreshToken(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return LoginResult.builder()
                .token(tokenInfo.getTokenValue())
                .expiresIn(tokenInfo.getTokenTimeout() * 1000L)
                .build();
    }

    @Override
    public void logout(String token) {
        StpUtil.logout();
    }
}
