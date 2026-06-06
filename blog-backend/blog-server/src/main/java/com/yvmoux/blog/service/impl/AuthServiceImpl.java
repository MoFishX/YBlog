package com.yvmoux.blog.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.dto.LoginResult;
import com.yvmoux.blog.dto.response.LoginVO;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.enums.RoleEnum;
import com.yvmoux.blog.enums.UserStatus;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.UserMapper;
import com.yvmoux.blog.security.AppUserDetails;
import com.yvmoux.blog.service.AsyncTaskService;
import com.yvmoux.blog.service.AuthService;
import com.yvmoux.blog.utils.JwtUtils;
import com.yvmoux.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final AsyncTaskService asyncTaskService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    @Override
    public void register(RegisterRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(BCrypt.hashpw(request.getPassword()))
                .email(request.getEmail())
                .avatar(null)
                .role(RoleEnum.USER.name())
                .status(UserStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        asyncTaskService.sendWelcomeEmail(user.getEmail());
    }

    @Override
    public LoginResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.generateAccessToken(
                userDetails.getUserId(), userDetails.getUsername(), userDetails.getAuthorities()
                        .iterator().next().getAuthority().replace("ROLE_", ""));
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUserId());

        LoginVO loginVO = LoginVO.builder()
                .id(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email("")
                .avatar(null)
                .role(userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
                .build();

        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400L)
                .user(loginVO)
                .build();
    }

    @Override
    public LoginResult refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String subject = jwtUtils.extractUsername(refreshToken);
        Long userId;
        try {
            userId = Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String accessToken = jwtUtils.generateAccessToken(userId, subject, "USER");
        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .expiresIn(86400L)
                .build();
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtUtils.validateToken(token)) {
            redisUtils.set("blacklist:token:" + token, "1", 86400, java.util.concurrent.TimeUnit.SECONDS);
        }
    }
}
