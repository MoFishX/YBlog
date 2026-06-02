package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.dto.response.LoginResponse;
import com.yvmoux.blog.dto.response.UserVO;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    LoginResponse refreshToken(Long userId);
    void logout(String token);
}
