package com.yvmoux.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("article_content")
public class ArticleContent {

    @TableId
    private Long articleId;

    private String content;
}
