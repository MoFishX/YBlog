package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.LoginResult;
import com.yvmoux.blog.dto.RefreshResult;
import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;

public interface AuthService {

    /** 登录：校验用户名密码，返回 Token + 用户信息 */
    LoginResult login(LoginRequest request);

    /** 注册：创建新用户（角色默认 USER），异步发欢迎邮件 */
    void register(RegisterRequest request);

    /** 刷新令牌：返回新的 access_token 及过期时间 */
    RefreshResult refreshToken(String refreshToken);

    /** 登出：Token 加入黑名单 */
    void logout(String token);
}
