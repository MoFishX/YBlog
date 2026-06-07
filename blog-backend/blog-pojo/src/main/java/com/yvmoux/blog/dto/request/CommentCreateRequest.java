package com.yvmoux.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentCreateRequest {
    private Long articleId;

    @NotBlank
    @Size(min = 1, max = 1000)
    private String content;

    private Long parentId;
}
