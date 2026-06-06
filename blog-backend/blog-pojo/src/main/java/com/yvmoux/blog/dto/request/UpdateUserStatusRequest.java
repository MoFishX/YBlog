package com.yvmoux.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Getter
public class UpdateUserStatusRequest {
    @NotBlank
    private String status;
}
