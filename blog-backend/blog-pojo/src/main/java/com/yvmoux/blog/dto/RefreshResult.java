package com.yvmoux.blog.dto;

import com.yvmoux.blog.dto.response.LoginVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResult {
    private String accessToken;
    private long expiresIn;
    private LoginVO user;
}