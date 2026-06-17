package dto

import "time"

// ArticleCreateRequest is used for creating articles.
type ArticleCreateRequest struct {
	Title            string   `json:"title" binding:"required,max=50"`
	Content          string   `json:"content" binding:"max=10000"`
	Summary          string   `json:"summary" binding:"max=300"`
	CoverImage       string   `json:"coverImage"`
	TagIDs           []int64  `json:"tagIds" binding:"max=10"`
	TagNames         []string `json:"tagNames" binding:"max=10"`
	Status           string   `json:"status"`
	GenAiSummary     int      `json:"genAiSummary"`
	GenAiSummaryLong int      `json:"genAiSummaryLong"`
}

// ArticleUpdateRequest is used for updating articles.
type ArticleUpdateRequest = ArticleCreateRequest

// ArticleQuery holds query parameters for article list.
type ArticleQuery struct {
	Page     int    `form:"page,default=1"`
	PageSize int    `form:"pageSize,default=10"`
	TagName  string `form:"tagName"`
	AuthorID int64  `form:"authorId"`
	OrderBy  string `form:"orderBy,default=latest"`
	Status   string `form:"status"`
	Keyword  string `form:"keyword"`
}

// HotArticleQuery holds query parameters for hot articles.
type HotArticleQuery struct {
	Limit int `form:"limit,default=10"`
}

// SearchArticleQuery holds query parameters for article search.
type SearchArticleQuery struct {
	Keyword  string `form:"keyword" binding:"required"`
	Page     int    `form:"page,default=1"`
	PageSize int    `form:"pageSize,default=10"`
}

// ArticleVO is the article response.
type ArticleVO struct {
	ID                int64     `json:"id"`
	Title             string    `json:"title"`
	Content           string    `json:"content,omitempty"`
	Summary           string    `json:"summary"`
	CoverImage        string    `json:"coverImage"`
	Status            string    `json:"status"`
	Author            *AuthorVO `json:"author"`
	Tags              []TagVO   `json:"tags"`
	ViewCount         int64     `json:"viewCount"`
	LikeCount         int64     `json:"likeCount"`
	CommentCount      int       `json:"commentCount"`
	IsLiked           bool      `json:"isLiked"`
	CreatedAt         time.Time `json:"createdAt"`
	UpdatedAt         time.Time `json:"updatedAt"`
	AiSummary         string    `json:"aiSummary"`
	AiSummaryStatus   int       `json:"aiSummaryStatus"`
	AiSummaryLong     string    `json:"aiSummaryLong"`
	AiSummaryLongStatus int     `json:"aiSummaryLongStatus"`
}

// AiSummaryLongVO is the long AI summary response.
type AiSummaryLongVO struct {
	Status     int    `json:"status"`
	SummaryLong string `json:"summaryLong"`
}
