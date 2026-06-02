package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        return tags.stream().map(tag -> TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(tag.getArticleCount())
                .build()).collect(Collectors.toList());
    }

    @Override
    public TagVO createTag(String name) {
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }

        Tag tag = new Tag();
        tag.setName(name);
        tagMapper.insert(tag);

        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(0)
                .build();
    }

    @Override
    public TagVO updateTag(Long tagId, String name) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.ne("id", tagId);
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }

        tag.setName(name);
        tagMapper.updateById(tag);

        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(tag.getArticleCount())
                .build();
    }

    @Override
    public void deleteTag(Long tagId) {
        articleTagMapper.deleteByTagId(tagId);
        tagMapper.deleteById(tagId);
    }
}
