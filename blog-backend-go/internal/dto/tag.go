package dto

// TagRequest is the payload for creating/updating tags.
type TagRequest struct {
	Name string `json:"name" binding:"required,min=1,max=30"`
}

// TagVO is the tag response.
type TagVO struct {
	ID          int64  `json:"id"`
	Name        string `json:"name"`
	ArticleCount int   `json:"articleCount"`
	CreatedBy   int64  `json:"createdBy,omitempty"`
}
