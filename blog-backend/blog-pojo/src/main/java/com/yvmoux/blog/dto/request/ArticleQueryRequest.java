package com.yvmoux.blog.dto.request;

import lombok.Data;
import lombok.Getter;

@Getter
public class ArticleQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 10;
    private Long tagId;
    private String orderBy;
    private String status;
    private String keyword;
}
