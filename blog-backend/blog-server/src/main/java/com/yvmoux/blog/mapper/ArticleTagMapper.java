package com.yvmoux.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yvmoux.blog.entity.ArticleTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    void insertBatch(@Param("list") List<ArticleTag> list);

    @Delete("DELETE FROM article_tag WHERE article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);

    @Delete("DELETE FROM article_tag WHERE tag_id = #{tagId}")
    void deleteByTagId(@Param("tagId") Long tagId);
}
