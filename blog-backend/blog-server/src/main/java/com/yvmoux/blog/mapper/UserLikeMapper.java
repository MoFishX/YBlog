package com.yvmoux.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yvmoux.blog.entity.UserLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserLikeMapper extends BaseMapper<UserLike> {

    @Select("SELECT COUNT(*) FROM user_like WHERE user_id = #{userId} AND article_id = #{articleId}")
    int countByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    @Select("SELECT COUNT(*) FROM user_like WHERE article_id = #{articleId}")
    int countByArticleId(@Param("articleId") Long articleId);
}
