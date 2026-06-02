package com.yvmoux.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsVO {
    private Long userCount;
    private Long articleCount;
    private Long commentCount;
    private Long totalViews;
    private Long totalLikes;
    private Long todayViews;
    private Long todayNewUsers;
    private Long todayNewArticles;
}
