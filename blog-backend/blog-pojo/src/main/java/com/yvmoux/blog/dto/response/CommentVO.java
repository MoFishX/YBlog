package com.yvmoux.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {
    private Long id;
    private String content;
    private AuthorVO user;
    private AuthorVO replyTo;
    private Long articleId;
    private String articleTitle;
    private Boolean isRead;
    private String status;
    private LocalDateTime createdAt;
}
