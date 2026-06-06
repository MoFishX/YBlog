package com.yvmoux.blog.converter;

import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TagConverter {

    public TagVO toTagVO(Tag tag) {
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(tag.getArticleCount())
                .build();
    }

    public List<TagVO> toTagVOList(List<Tag> tags) {
        return tags.stream().map(this::toTagVO).collect(Collectors.toList());
    }
}
