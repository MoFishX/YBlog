package com.yvmoux.blog.converter;

import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.AuthorVO;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArticleConverter {

    public ArticleVO toArticleVO(Article article, User author, List<TagVO> tags, Integer commentCount, String content) {

        ArticleVO vo = ArticleVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(content)
                .summary(article.getSummary())
                .coverImage(article.getCoverImage())
                .status(article.getStatus())
                .author(null)
                .tags(tags)
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(commentCount)
                .isLiked(false) // TODO: 获取当前用户是否点赞
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();

        if (author != null) {
            AuthorVO a = AuthorVO.builder().id(author.getId())
                    .username(author.getUsername())
                    .avatar(author.getAvatar())
                    .email(author.getEmail())
                    .build();
            vo.setAuthor(a);
        }

        return vo;
    }
}
