package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.PageResult;

public interface SearchService {
    PageResult<ArticleVO> search(String keyword, int page, int pageSize);
}
