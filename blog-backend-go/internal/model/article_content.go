package model

// ArticleContent maps to the `article_content` table.
type ArticleContent struct {
	ArticleID int64  `gorm:"column:article_id;primaryKey" json:"articleId"`
	Content   string `gorm:"column:content;type:longtext;not null" json:"content"`
}

func (ArticleContent) TableName() string {
	return "article_content"
}
