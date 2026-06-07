package com.yvmoux.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleVO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String status;
    private AuthorVO author;
    private List<TagVO> tags;
    private Long viewCount;
    private Long likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String aiSummary;
    private Integer aiSummaryStatus;
}
