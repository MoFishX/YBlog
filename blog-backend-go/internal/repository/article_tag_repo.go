package repository

import (
	"context"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"gorm.io/gorm"
)

// ArticleTagRepo handles article-tag associations.
type ArticleTagRepo struct {
	db *gorm.DB
}

// NewArticleTagRepo creates an ArticleTagRepo.
func NewArticleTagRepo(db *gorm.DB) *ArticleTagRepo {
	return &ArticleTagRepo{db: db}
}

// InsertBatch inserts multiple associations in a batch.
func (r *ArticleTagRepo) InsertBatch(ctx context.Context, articleID int64, tagIDs []int64) error {
	if len(tagIDs) == 0 {
		return nil
	}
	var records []model.ArticleTag
	for _, tagID := range tagIDs {
		records = append(records, model.ArticleTag{ArticleID: articleID, TagID: tagID})
	}
	return r.db.WithContext(ctx).CreateInBatches(records, 100).Error
}

// DeleteByArticleID deletes associations by article id.
func (r *ArticleTagRepo) DeleteByArticleID(ctx context.Context, articleID int64) error {
	return r.db.WithContext(ctx).Where("article_id = ?", articleID).Delete(&model.ArticleTag{}).Error
}

// DeleteByTagID deletes associations by tag id.
func (r *ArticleTagRepo) DeleteByTagID(ctx context.Context, tagID int64) error {
	return r.db.WithContext(ctx).Where("tag_id = ?", tagID).Delete(&model.ArticleTag{}).Error
}

// DeleteByArticleIDs deletes associations for multiple article ids within a transaction.
func (r *ArticleTagRepo) DeleteByArticleIDs(ctx context.Context, ids []int64) error {
	return r.db.WithContext(ctx).Where("article_id IN ?", ids).Delete(&model.ArticleTag{}).Error
}
