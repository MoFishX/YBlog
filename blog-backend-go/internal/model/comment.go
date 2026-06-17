package model

import "time"

// Comment maps to the `comment` table.
type Comment struct {
	ID        int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	ArticleID int64     `gorm:"column:article_id;not null;index:idx_article" json:"articleId"`
	UserID    int64     `gorm:"column:user_id;not null" json:"userId"`
	Content   string    `gorm:"column:content;type:text;not null" json:"content"`
	ParentID  *int64    `gorm:"column:parent_id" json:"parentId"`
	Status    string    `gorm:"column:status;size:20;not null;default:ACTIVE" json:"status"`
	IsRead    int       `gorm:"column:is_read;not null;default:0" json:"isRead"`
	CreatedAt time.Time `gorm:"column:created_at;not null" json:"createdAt"`
}

func (Comment) TableName() string {
	return "comment"
}
