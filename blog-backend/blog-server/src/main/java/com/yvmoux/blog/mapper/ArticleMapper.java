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

    @Select("SELECT COUNT(*) FROM article")
    long countAll();

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM article")
    long sumViewCount();

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM article")
    long sumLikeCount();

    @Select("SELECT COUNT(*) FROM article WHERE DATE(created_at) = CURDATE()")
    long countToday();

    @Select("SELECT a.*, MATCH(a.title, a.summary) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS relevance " +
            "FROM article a LEFT JOIN article_content ac ON a.id = ac.article_id " +
            "WHERE a.status = 'PUBLISHED' " +
            "AND (MATCH(a.title, a.summary) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "OR MATCH(ac.content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)) " +
            "ORDER BY relevance DESC LIMIT #{offset}, #{pageSize}")
    List<Article> searchByKeyword(@Param("keyword") String keyword,
                                   @Param("offset") int offset,
                                   @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM article a LEFT JOIN article_content ac ON a.id = ac.article_id " +
            "WHERE a.status = 'PUBLISHED' " +
            "AND (MATCH(a.title, a.summary) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "OR MATCH(ac.content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE))")
    long countSearch(@Param("keyword") String keyword);
}
