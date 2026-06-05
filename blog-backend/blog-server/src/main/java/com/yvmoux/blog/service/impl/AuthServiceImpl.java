package com.yvmoux.blog.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.dto.LoginResult;
import com.yvmoux.blog.dto.response.LoginVO;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.enums.RoleEnum;
import com.yvmoux.blog.enums.UserStatus;
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
        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(BCrypt.hashpw(request.getPassword())) // 使用 BCrypt 对密码进行加密
                .email(request.getEmail())
                .avatar(null)
                .role(RoleEnum.USER.name())
                .status(UserStatus.ACTIVE.name())
                .articleCount(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 插入用户到数据库
        userMapper.insert(user);

        // 发送欢迎邮件
        asyncTaskService.sendWelcomeEmail(user.getEmail());
    }

    @Override
    public LoginResult login(LoginRequest request) {
        // 根据 username 唯一约束查询用户，selectOne 确保最多返回一行
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        if (UserStatus.BANNED.name().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        // 登录并将角色存入 Sa-Token Session，供后续 @SaCheckRole 使用
        StpUtil.login(user.getId());
        StpUtil.getSession().set("role", user.getRole());

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginVO loginVO = LoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();

        return LoginResult.builder()
                .token(tokenInfo.getTokenValue())
                .expiresIn(tokenInfo.getTokenTimeout())
                .user(loginVO)
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
                .expiresIn(tokenInfo.getTokenTimeout())
                .build();
    }

    @Override
    public void logout(String token) {
        StpUtil.logout();
    }
}
