package model

// Tag maps to the `tag` table.
type Tag struct {
	ID          int64  `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	Name        string `gorm:"column:name;size:50;not null;uniqueIndex:uk_name" json:"name"`
	CreatedBy   int64  `gorm:"column:created_by;not null" json:"createdBy"`
	ArticleCount int   `gorm:"-" json:"articleCount"`
}

func (Tag) TableName() string {
	return "tag"
}
