package model

import "time"

// UserLike maps to the `user_like` table.
type UserLike struct {
	ID        int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	UserID    int64     `gorm:"column:user_id;not null" json:"userId"`
	ArticleID int64     `gorm:"column:article_id;not null" json:"articleId"`
	CreatedAt time.Time `gorm:"column:created_at;not null" json:"createdAt"`
}

func (UserLike) TableName() string {
	return "user_like"
}
