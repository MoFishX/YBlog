package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.dto.LoginResult;

public interface AuthService {
    LoginResult login(LoginRequest request);
    void register(RegisterRequest request);
    LoginResult refreshToken(Long userId);
    void logout(String token);
}
