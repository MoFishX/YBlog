package model

import "time"

// Article maps to the `article` table.
type Article struct {
	ID           int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	Title        string    `gorm:"column:title;size:200;not null" json:"title"`
	Summary      string    `gorm:"column:summary;size:500" json:"summary"`
	AiSummary    string    `gorm:"column:ai_summary;type:longtext" json:"aiSummary"`
	AiSummaryLong string   `gorm:"column:ai_summary_long;type:longtext" json:"aiSummaryLong"`
	CoverImage   string    `gorm:"column:cover_image;size:255" json:"coverImage"`
	AuthorID     int64     `gorm:"column:author_id;not null;index:idx_author" json:"authorId"`
	Status       string    `gorm:"column:status;size:20;not null;default:PUBLISHED" json:"status"`
	ViewCount    int64     `gorm:"column:view_count;not null;default:0" json:"viewCount"`
	LikeCount    int64     `gorm:"column:like_count;not null;default:0" json:"likeCount"`
	CreatedAt    time.Time `gorm:"column:created_at;not null;index:idx_created" json:"createdAt"`
	UpdatedAt    time.Time `gorm:"column:updated_at;not null" json:"updatedAt"`
}

func (Article) TableName() string {
	return "article"
}
