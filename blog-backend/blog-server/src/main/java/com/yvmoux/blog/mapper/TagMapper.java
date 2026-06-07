package com.yvmoux.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yvmoux.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("select " +
            "t.id as id," +
            "t.name as name," +
            "count(distinct at.article_id) as article_count " +
                "from tag as t " +
                    "left join article_tag as at on t.id = at.tag_id " +
                        "group by t.id, t.name " +
                        "order by t.id;")
    List<Tag> selectAllWithArticleCount();

    @Select("SELECT t.* FROM tag t INNER JOIN article_tag at ON t.id = at.tag_id WHERE at.article_id = #{articleId}")
    List<Tag> selectByArticleId(@Param("articleId") Long articleId);


}
