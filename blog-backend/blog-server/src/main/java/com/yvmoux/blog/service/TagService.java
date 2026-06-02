package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.response.TagVO;
import java.util.List;

public interface TagService {
    List<TagVO> getAllTags();
    TagVO createTag(String name);
    TagVO updateTag(Long tagId, String name);
    void deleteTag(Long tagId);
}
