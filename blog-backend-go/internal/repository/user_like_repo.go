package repository

import (
	"context"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"gorm.io/gorm"
)

// UserLikeRepo handles user like data access.
type UserLikeRepo struct {
	db *gorm.DB
}

// NewUserLikeRepo creates a UserLikeRepo.
func NewUserLikeRepo(db *gorm.DB) *UserLikeRepo {
	return &UserLikeRepo{db: db}
}

// Create inserts a like record.
func (r *UserLikeRepo) Create(ctx context.Context, like *model.UserLike) error {
	return r.db.WithContext(ctx).Create(like).Error
}

// Delete deletes a like record by user and article.
func (r *UserLikeRepo) Delete(ctx context.Context, userID, articleID int64) error {
	return r.db.WithContext(ctx).Where("user_id = ? AND article_id = ?", userID, articleID).Delete(&model.UserLike{}).Error
}

// CountByUserAndArticle returns whether the user liked the article.
func (r *UserLikeRepo) CountByUserAndArticle(ctx context.Context, userID, articleID int64) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.UserLike{}).Where("user_id = ? AND article_id = ?", userID, articleID).Count(&count).Error
	return count, err
}

// CountByArticleID counts likes for an article.
func (r *UserLikeRepo) CountByArticleID(ctx context.Context, articleID int64) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.UserLike{}).Where("article_id = ?", articleID).Count(&count).Error
	return count, err
}

// DeleteByArticleIDs deletes likes for multiple articles within a transaction.
func (r *UserLikeRepo) DeleteByArticleIDs(ctx context.Context, ids []int64) error {
	return r.db.WithContext(ctx).Where("article_id IN ?", ids).Delete(&model.UserLike{}).Error
}
