package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.response.TagVO;
import java.util.List;

public interface TagService {

    /**
     * 获取所有标签
     *
     * @return 所有标签信息
     */
    List<TagVO> getAllTags();

    /**
     * 创建标签
     *
     * @param name       标签名称
     * @param createdBy  创建者用户ID
     * @return 创建的标签信息
     */
    TagVO createTag(String name, Long createdBy);

    /**
     * 更新标签信息
     *
     * @param tagId 标签ID
     * @param name  标签名称
     * @return 更新后的标签信息
     */
    TagVO updateTag(Long tagId, String name);

    /**
     * 删除标签
     *
     * @param tagId 标签ID
     */
    void deleteTag(Long tagId);

    /**
     * 解析标签名称列表，返回对应的标签ID列表。
     * 如果标签不存在，则创建新标签。
     *
     * @param tagNames 标签名称列表
     * @param userId   创建者用户ID
     * @return 标签ID列表
     */
    List<Long> resolveTagIds(List<String> tagNames, Long userId);
}
