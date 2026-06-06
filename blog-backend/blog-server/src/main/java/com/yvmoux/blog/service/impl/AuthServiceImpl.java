package com.yvmoux.blog.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yvmoux.blog.dto.LoginResult;
import com.yvmoux.blog.dto.RefreshResult;
import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
        // 1. 认证：AuthenticationManager 内部调 AppUserDetailsService 查用户 + BCrypt 比对密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // 2. 获取自定义 UserDetails，取得 userId（标准接口没有此字段）
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        // 3. 签发短期 access_token（payload 存 userId + username + role）
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String accessToken = jwtUtils.generateAccessToken(
                userDetails.getUserId(),
                userDetails.getUsername(),
                Objects.requireNonNull(
                        userDetails.getAuthorities().iterator().next().getAuthority()).replace("ROLE_", "")
        );

        // 4. 签发长期 refresh_token（只存 userId）
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUserId());

        // 5. 构建返回的用户信息
        LoginVO loginVO = LoginVO.builder()
                .id(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email("")
                .avatar(null)
                .role(userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
                .build();

        // 6. 打包返回
        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .user(loginVO)
                .build();
    }

    @Override
    public RefreshResult refreshToken(String refreshToken) {
        // 1. 验证 refresh_token 是否有效
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 从 refresh_token 提取 userId
        String subject = jwtUtils.extractUsername(refreshToken);
        Long userId = Long.parseLong(subject);

        // 3. 从 DB 获取用户最新信息（角色可能已变更）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 4. 签发新的 access_token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        return RefreshResult.builder()
                .accessToken(accessToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .build();
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtUtils.validateToken(token)) {
            redisUtils.set("blacklist:token:" + token, "1", jwtUtils.getAccessTokenExpiration(), TimeUnit.SECONDS);
        }
    }
}
