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
        // 查询所有标签及对应的文章数文章数
        List<Tag> tags = tagMapper.selectAllWithArticleCount();

        // 构建返回值
        return tags.stream().map(tag -> TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(tag.getArticleCount())
                .build()).collect(Collectors.toList());
    }

    @Override
    public TagVO createTag(String name) {
        // 检查标签名是否存在
        if (tagMapper.selectCount(new QueryWrapper<Tag>().eq("name", name)) > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }

        // 插入标签
        Tag tag = Tag.builder().name(name).build();
        tagMapper.insert(tag);

        // 构建返回值
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(0)
                .build();
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
                .build();
    }

    @Override
    public void deleteTag(Long tagId) {
        // 删除标签关联及标签本身
        articleTagMapper.deleteByTagId(tagId);
        tagMapper.deleteById(tagId);
    }
}
