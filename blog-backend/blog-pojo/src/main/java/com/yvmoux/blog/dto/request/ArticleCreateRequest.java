package com.yvmoux.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ArticleCreateRequest {
    @NotBlank
    @Size(min = 1, max = 200)
    private String title;

    private String content;

    private String summary;
    private String coverImage;
    private List<Long> tagIds;
    private String status;
}
