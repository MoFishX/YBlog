package com.yvmoux.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yvmoux.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Update("UPDATE article SET view_count = view_count + #{incrCount} WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id, @Param("incrCount") Long incrCount);

    @Update("UPDATE article SET like_count = like_count + #{incrCount} WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id, @Param("incrCount") Long incrCount);

    @Select("SELECT COUNT(*) FROM article WHERE deleted_at IS NULL")
    long countAll();

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM article WHERE deleted_at IS NULL")
    long sumViewCount();

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM article WHERE deleted_at IS NULL")
    long sumLikeCount();

    @Select("SELECT COUNT(*) FROM article WHERE deleted_at IS NULL AND DATE(created_at) = CURDATE()")
    long countToday();

//    List<Article> getListByCondition(Integer page, Integer pageSize, String tagName, String orderBy, String status, Long userId);
}
