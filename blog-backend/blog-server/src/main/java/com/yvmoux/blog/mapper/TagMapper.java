package com.yvmoux.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yvmoux.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("SELECT t.*, COUNT(at.tag_id) AS article_count FROM tag t LEFT JOIN article_tag at ON t.id = at.tag_id GROUP BY t.id HAVING COUNT(at.tag_id) > 0 ORDER BY article_count DESC")
    List<Tag> selectAllWithArticleCount();

    @Select("SELECT t.* FROM tag t INNER JOIN article_tag at ON t.id = at.tag_id WHERE at.article_id = #{articleId}")
    List<Tag> selectByArticleId(@Param("articleId") Long articleId);
}
