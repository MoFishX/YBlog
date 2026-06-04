package com.yvmoux.blog.dto;

import com.yvmoux.blog.dto.response.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {
    private String token;
    private long expiresIn;
    private UserVO user;
}
