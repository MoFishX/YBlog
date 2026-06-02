package com.yvmoux.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yvmoux.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT COUNT(*) FROM comment WHERE article_id = #{articleId}")
    int countByArticleId(@Param("articleId") Long articleId);

    @Select("SELECT COUNT(*) FROM comment")
    long countAll();
}
