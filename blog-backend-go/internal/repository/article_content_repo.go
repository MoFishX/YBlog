package repository

import (
	"context"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"gorm.io/gorm"
)

// ArticleContentRepo handles article content data access.
type ArticleContentRepo struct {
	db *gorm.DB
}

// NewArticleContentRepo creates an ArticleContentRepo.
func NewArticleContentRepo(db *gorm.DB) *ArticleContentRepo {
	return &ArticleContentRepo{db: db}
}

// Create inserts article content.
func (r *ArticleContentRepo) Create(ctx context.Context, content *model.ArticleContent) error {
	return r.db.WithContext(ctx).Create(content).Error
}

// GetByArticleID retrieves content by article id.
func (r *ArticleContentRepo) GetByArticleID(ctx context.Context, articleID int64) (*model.ArticleContent, error) {
	var content model.ArticleContent
	err := r.db.WithContext(ctx).First(&content, articleID).Error
	if err == gorm.ErrRecordNotFound {
		return nil, nil
	}
	return &content, err
}

// Update updates article content.
func (r *ArticleContentRepo) Update(ctx context.Context, content *model.ArticleContent) error {
	return r.db.WithContext(ctx).Save(content).Error
}

// DeleteByArticleIDs deletes content by article ids.
func (r *ArticleContentRepo) DeleteByArticleIDs(ctx context.Context, ids []int64) error {
	return r.db.WithContext(ctx).Where("article_id IN ?", ids).Delete(&model.ArticleContent{}).Error
}
