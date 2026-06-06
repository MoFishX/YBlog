package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.request.LoginRequest;
import com.yvmoux.blog.dto.request.RegisterRequest;
import com.yvmoux.blog.dto.LoginResult;

public interface AuthService {

    /** 登录：校验用户名密码，返回 Token + 用户信息 */
    LoginResult login(LoginRequest request);

    /** 注册：创建新用户（角色默认 USER），异步发欢迎邮件 */
    void register(RegisterRequest request);

    /** 刷新令牌：返回当前有效的 Token 及剩余有效时间 */
    LoginResult refreshToken(String refreshToken);

    /** 登出：Token 加入黑名单或直接销毁登录状态 */
    void logout(String token);
}
