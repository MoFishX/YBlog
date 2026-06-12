package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.Tag;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.ArticleTagMapper;
import com.yvmoux.blog.mapper.TagMapper;
import com.yvmoux.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    public List<TagVO> getAllTags() {
        List<Tag> tags = tagMapper.selectAllWithArticleCount();

        return tags.stream()
                .map(tag -> TagVO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .articleCount(tag.getArticleCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<TagVO> getAllTags(Integer page, Integer pageSize, boolean includeCreatedBy) {
        List<Tag> allTags = tagMapper.selectAllWithArticleCount();

        int total = allTags.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Tag> paged = fromIndex < total ? allTags.subList(fromIndex, toIndex) : List.of();

        List<TagVO> records = paged.stream()
                .map(tag -> {
                    TagVO.TagVOBuilder builder = TagVO.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .articleCount(tag.getArticleCount());
                    if (includeCreatedBy) {
                        builder.createdBy(tag.getCreatedBy());
                    }
                    return builder.build();
                })
                .collect(Collectors.toList());

        return new PageResult<>(records, total);
    }

    @Override
    public TagVO createTag(String name, Long createdBy) {
        // 检查标签名是否存在
        if (tagMapper.selectCount(new QueryWrapper<Tag>().eq("name", name)) > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }

        // 插入标签
        Tag tag = Tag.builder().name(name).createdBy(createdBy).build();
        tagMapper.insert(tag);

        // 构建返回值
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(0)
                .createdBy(tag.getCreatedBy())
                .build();
    }

    @Override
    public List<Long> resolveTagIds(List<String> tagNames, Long userId) {
        if (tagNames == null || tagNames.isEmpty()) return new ArrayList<>();

        List<Long> ids = new ArrayList<>();
        for (String name : tagNames) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) continue;

            Tag existing = tagMapper.selectOne(new QueryWrapper<Tag>().eq("name", trimmed));
            if (existing != null) {
                ids.add(existing.getId());
            } else {
                Tag tag = Tag.builder().name(trimmed).createdBy(userId).build();
                tagMapper.insert(tag);
                ids.add(tag.getId());
            }
        }
        return ids;
    }

    @Override
    public TagVO updateTag(Long tagId, String name) {
        // 获取标签
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 检查标签名是否被其他标签占用
        if (tagMapper.selectCount(new QueryWrapper<Tag>().eq("name", name).ne("id", tagId)) > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }

        // 更新标签
        tag.setName(name);
        tagMapper.updateById(tag);

        // 构建返回值
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(tag.getArticleCount())
                .createdBy(tag.getCreatedBy())
                .build();
    }

    @Override
    public void deleteTag(Long tagId) {
        // 删除标签关联及标签本身
        articleTagMapper.deleteByTagId(tagId);
        tagMapper.deleteById(tagId);
    }
}
