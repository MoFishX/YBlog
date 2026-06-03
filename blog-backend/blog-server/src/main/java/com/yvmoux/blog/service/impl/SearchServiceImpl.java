package com.yvmoux.blog.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.yvmoux.blog.dto.response.ArticleVO;
import com.yvmoux.blog.dto.response.AuthorVO;
import com.yvmoux.blog.dto.response.PageResult;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.entity.Tag;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.mapper.ArticleMapper;
import com.yvmoux.blog.mapper.TagMapper;
import com.yvmoux.blog.mapper.UserMapper;
import com.yvmoux.blog.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;

    @Override
    public PageResult<ArticleVO> search(String keyword, int page, int pageSize) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("articles")
                    .query(q -> q
                            .multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields("title^3", "content")
                            )
                    )
                    .highlight(h -> h
                            .fields("title", hf -> hf
                                    .fragmentSize(100)
                                    .numberOfFragments(1)
                                    .preTags("<em>")
                                    .postTags("</em>")
                            )
                            .fields("content", hf -> hf
                                    .fragmentSize(200)
                                    .numberOfFragments(1)
                                    .preTags("<em>")
                                    .postTags("</em>")
                            )
                    )
                    .from((page - 1) * pageSize)
                    .size(pageSize)
            );

            SearchResponse<Map> searchResponse = elasticsearchClient.search(searchRequest, Map.class);

            List<ArticleVO> records = new ArrayList<>();
            for (Hit<Map> hit : searchResponse.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source == null) {
                    continue;
                }

                Long articleId = toLong(source.get("id"));
                Long authorId = toLong(source.get("authorId"));

                String titleHighlight = null;
                String contentHighlight = null;
                Map<String, List<String>> highlights = hit.highlight();
                if (highlights != null) {
                    List<String> titleHL = highlights.get("title");
                    if (titleHL != null && !titleHL.isEmpty()) {
                        titleHighlight = titleHL.get(0);
                    }
                    List<String> contentHL = highlights.get("content");
                    if (contentHL != null && !contentHL.isEmpty()) {
                        contentHighlight = contentHL.get(0);
                    }
                }

                User author = userMapper.selectById(authorId);
                List<Tag> tags = tagMapper.selectByArticleId(articleId);
                List<TagVO> tagVOs = tags != null ? tags.stream()
                        .map(t -> TagVO.builder().id(t.getId()).name(t.getName()).build())
                        .collect(Collectors.toList()) : new ArrayList<>();

                ArticleVO vo = ArticleVO.builder()
                        .id(articleId)
                        .title((String) source.get("title"))
                        .summary((String) source.get("summary"))
                        .viewCount(toLong(source.get("viewCount")))
                        .likeCount(toLong(source.get("likeCount")))
                        .titleHighlight(titleHighlight)
                        .contentHighlight(contentHighlight)
                        .tags(tagVOs)
                        .build();

                if (author != null) {
                    vo.setAuthor(AuthorVO.builder()
                            .id(author.getId())
                            .username(author.getUsername())
                            .avatar(author.getAvatar())
                            .email(author.getEmail())
                            .build());
                }

                Object createdAt = source.get("createdAt");
                if (createdAt != null) {
                    try {
                        vo.setCreatedAt(java.time.LocalDateTime.parse(createdAt.toString()));
                    } catch (Exception e) {
                        log.debug("Failed to parse createdAt: {}", createdAt);
                    }
                }

                records.add(vo);
            }

            long total = searchResponse.hits().total() != null ? searchResponse.hits().total().value() : 0;
            return new PageResult<>(records, total, page, pageSize);
        } catch (Exception e) {
            log.error("ES search error for keyword: {}", keyword, e);
            return new PageResult<>(new ArrayList<>(), 0, page, pageSize);
        }
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
