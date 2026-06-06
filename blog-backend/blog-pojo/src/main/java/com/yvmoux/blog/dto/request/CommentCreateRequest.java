package com.yvmoux.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

@Getter
public class CommentCreateRequest {
    @NotBlank
    @Size(min = 1, max = 1000)
    private String content;

    private Long parentId;
}
