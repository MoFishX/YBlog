package com.yvmoux.blog.converter;

import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.AuthorVO;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.entity.Tag;
import com.yvmoux.blog.entity.User;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TagConverter {

    default TagVO toTagVO(Tag tag) {
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .articleCount(tag.getArticleCount())
                .build();
    }

    default List<TagVO> toTagVOList(List<Tag> tags) {
        return tags.stream().map(this::toTagVO).collect(Collectors.toList());
    }
}
