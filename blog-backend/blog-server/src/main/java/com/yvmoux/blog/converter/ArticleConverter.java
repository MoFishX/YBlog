package com.yvmoux.blog.converter;

import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.AuthorVO;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.Article;
import com.yvmoux.blog.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArticleConverter {

    default ArticleVO toArticleVO(Article article, User author, List<TagVO> tags, Integer commentCount) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setStatus(article.getStatus());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setIsLiked(false);
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());
        vo.setTags(tags);
        vo.setCommentCount(commentCount);
        if (author != null) {
            AuthorVO authorVO = new AuthorVO();
            authorVO.setId(author.getId());
            authorVO.setUsername(author.getUsername());
            authorVO.setAvatar(author.getAvatar());
            vo.setAuthor(authorVO);
        }
        return vo;
    }
}
