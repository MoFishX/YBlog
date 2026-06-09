package com.yvmoux.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class ArticleCommonCU {
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    @Size(max = 10000)
    private String content;

    @Size(max = 300)
    private String summary;

    private String coverImage;

    @Size(min = 1, max = 10)
    private List<Long> tagIds;

    private String status;

    private Integer genAiSummary;

    private Integer genAiSummaryLong;
}
