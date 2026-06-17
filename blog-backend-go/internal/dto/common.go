package dto

import "time"

// AuthorVO represents a lightweight user in article/comment responses.
type AuthorVO struct {
	ID       int64  `json:"id"`
	Username string `json:"username"`
	Avatar   string `json:"avatar"`
	Email    string `json:"email,omitempty"`
}

// StatsVO is the admin dashboard statistics response.
type StatsVO struct {
	UserCount       int64 `json:"userCount"`
	ArticleCount    int64 `json:"articleCount"`
	CommentCount    int64 `json:"commentCount"`
	TotalViews      int64 `json:"totalViews"`
	TotalLikes      int64 `json:"totalLikes"`
	TodayViews      int64 `json:"todayViews"`
	TodayNewUsers   int64 `json:"todayNewUsers"`
	TodayNewArticles int64 `json:"todayNewArticles"`
}

// TrendVO represents a daily view trend point.
type TrendVO struct {
	Date  string `json:"date"`
	Count int64  `json:"count"`
}

// UploadResult is returned after a successful file upload.
type UploadResult struct {
	URL      string `json:"url"`
	Filename string `json:"filename"`
	Size     int64  `json:"size"`
}

// UploadRequest holds the upload form data.
type UploadRequest struct {
	Type string `form:"type" binding:"required"`
}

// DateStatsKey helper to format Redis stats keys by date.
type DateStatsKey struct {
	Date time.Time
}
