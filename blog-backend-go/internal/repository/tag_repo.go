package repository

import (
	"context"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"gorm.io/gorm"
)

// TagRepo handles tag data access.
type TagRepo struct {
	db *gorm.DB
}

// NewTagRepo creates a TagRepo.
func NewTagRepo(db *gorm.DB) *TagRepo {
	return &TagRepo{db: db}
}

// Create inserts a tag.
func (r *TagRepo) Create(ctx context.Context, tag *model.Tag) error {
	return r.db.WithContext(ctx).Create(tag).Error
}

// GetByID retrieves a tag by id.
func (r *TagRepo) GetByID(ctx context.Context, id int64) (*model.Tag, error) {
	var tag model.Tag
	err := r.db.WithContext(ctx).First(&tag, id).Error
	if err == gorm.ErrRecordNotFound {
		return nil, nil
	}
	return &tag, err
}

// GetByName retrieves a tag by name.
func (r *TagRepo) GetByName(ctx context.Context, name string) (*model.Tag, error) {
	var tag model.Tag
	err := r.db.WithContext(ctx).Where("name = ?", name).First(&tag).Error
	if err == gorm.ErrRecordNotFound {
		return nil, nil
	}
	return &tag, err
}

// Update updates a tag.
func (r *TagRepo) Update(ctx context.Context, tag *model.Tag) error {
	return r.db.WithContext(ctx).Save(tag).Error
}

// DeleteByID deletes a tag by id.
func (r *TagRepo) DeleteByID(ctx context.Context, id int64) error {
	return r.db.WithContext(ctx).Delete(&model.Tag{}, id).Error
}

// CountByName counts tags with the given name excluding id.
func (r *TagRepo) CountByName(ctx context.Context, name string, excludeID int64) (int64, error) {
	var count int64
	db := r.db.WithContext(ctx).Model(&model.Tag{}).Where("name = ?", name)
	if excludeID > 0 {
		db = db.Where("id != ?", excludeID)
	}
	err := db.Count(&count).Error
	return count, err
}

// ListAllWithArticleCount lists all tags with their article counts.
func (r *TagRepo) ListAllWithArticleCount(ctx context.Context) ([]model.Tag, error) {
	var tags []model.Tag
	err := r.db.WithContext(ctx).Raw(`
		SELECT t.id, t.name, t.created_by, COUNT(DISTINCT at.article_id) AS article_count
		FROM tag t
		LEFT JOIN article_tag at ON t.id = at.tag_id
		GROUP BY t.id, t.name, t.created_by
		ORDER BY t.id
	`).Scan(&tags).Error
	return tags, err
}

// ListByArticleID lists tags associated with an article.
func (r *TagRepo) ListByArticleID(ctx context.Context, articleID int64) ([]model.Tag, error) {
	var tags []model.Tag
	err := r.db.WithContext(ctx).Raw(`
		SELECT t.* FROM tag t
		INNER JOIN article_tag at ON t.id = at.tag_id
		WHERE at.article_id = ?
	`, articleID).Scan(&tags).Error
	return tags, err
}

// TagArticleCount returns the number of articles using a tag.
func (r *TagRepo) TagArticleCount(ctx context.Context, tagID int64) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.ArticleTag{}).Where("tag_id = ?", tagID).Count(&count).Error
	return count, err
}

// NormalizeName normalizes a tag name. Currently trims whitespace.
func NormalizeName(name string) string {
	return constant.Trim(name)
}
