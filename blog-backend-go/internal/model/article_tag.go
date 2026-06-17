package model

// ArticleTag maps to the `article_tag` association table.
type ArticleTag struct {
	ArticleID int64 `gorm:"column:article_id;primaryKey" json:"articleId"`
	TagID     int64 `gorm:"column:tag_id;primaryKey" json:"tagId"`
}

func (ArticleTag) TableName() string {
	return "article_tag"
}
