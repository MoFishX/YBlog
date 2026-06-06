package com.yvmoux.blog.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {
    @Email
    private String email;

    private String avatar;
}
