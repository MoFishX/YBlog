package dto

import "time"

// CommentCreateRequest is the payload for creating comments.
type CommentCreateRequest struct {
	ArticleID int64  `json:"articleId" binding:"required"`
	Content   string `json:"content" binding:"required,min=1,max=1000"`
	ParentID  *int64 `json:"parentId"`
}

// CommentQuery holds query parameters for article comments.
type CommentQuery struct {
	ArticleID int64 `form:"articleId" binding:"required"`
	Page      int   `form:"page,default=1"`
	PageSize  int   `form:"pageSize,default=20"`
}

// ReplyQuery holds query parameters for replies to the current user.
type ReplyQuery struct {
	Page      int `form:"page,default=1"`
	PageSize  int `form:"pageSize,default=10"`
	UnreadOnly int `form:"unreadOnly,default=0"`
}

// MyCommentQuery holds query parameters for the current user's comments.
type MyCommentQuery struct {
	Page     int `form:"page,default=1"`
	PageSize int `form:"pageSize,default=20"`
}

// AdminCommentQuery holds query parameters for admin comment list.
type AdminCommentQuery struct {
	Page     int    `form:"page,default=1"`
	PageSize int    `form:"pageSize,default=20"`
	Keyword  string `form:"keyword"`
	ArticleID int64 `form:"articleId"`
}

// CommentVO is the comment response.
type CommentVO struct {
	ID          int64     `json:"id"`
	Content     string    `json:"content"`
	User        *AuthorVO `json:"user"`
	ReplyTo     *AuthorVO `json:"replyTo"`
	ArticleID   int64     `json:"articleId"`
	ParentID    *int64    `json:"parentId"`
	ArticleTitle string   `json:"articleTitle,omitempty"`
	IsRead      bool      `json:"isRead"`
	Status      string    `json:"status"`
	CreatedAt   time.Time `json:"createdAt"`
}
