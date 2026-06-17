package repository

import (
	"context"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"gorm.io/gorm"
)

// CommentRepo handles comment data access.
type CommentRepo struct {
	db *gorm.DB
}

// NewCommentRepo creates a CommentRepo.
func NewCommentRepo(db *gorm.DB) *CommentRepo {
	return &CommentRepo{db: db}
}

// Create inserts a comment.
func (r *CommentRepo) Create(ctx context.Context, comment *model.Comment) error {
	return r.db.WithContext(ctx).Create(comment).Error
}

// GetByID retrieves a comment by id.
func (r *CommentRepo) GetByID(ctx context.Context, id int64) (*model.Comment, error) {
	var comment model.Comment
	err := r.db.WithContext(ctx).First(&comment, id).Error
	if err == gorm.ErrRecordNotFound {
		return nil, nil
	}
	return &comment, err
}

// Update updates a comment.
func (r *CommentRepo) Update(ctx context.Context, comment *model.Comment) error {
	return r.db.WithContext(ctx).Save(comment).Error
}

// DeleteByID deletes a comment by id.
func (r *CommentRepo) DeleteByID(ctx context.Context, id int64) error {
	return r.db.WithContext(ctx).Delete(&model.Comment{}, id).Error
}

// DeleteByArticleID deletes all comments for an article.
func (r *CommentRepo) DeleteByArticleID(ctx context.Context, articleID int64) error {
	return r.db.WithContext(ctx).Where("article_id = ?", articleID).Delete(&model.Comment{}).Error
}

// CountByArticleID counts comments for an article.
func (r *CommentRepo) CountByArticleID(ctx context.Context, articleID int64) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.Comment{}).Where("article_id = ?", articleID).Count(&count).Error
	return count, err
}

// CountAll returns total comment count.
func (r *CommentRepo) CountAll(ctx context.Context) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.Comment{}).Count(&count).Error
	return count, err
}

// ListByArticle lists comments for an article, optionally including hidden comments owned by userID.
func (r *CommentRepo) ListByArticle(ctx context.Context, articleID, userID int64, page, pageSize int) ([]model.Comment, int64, error) {
	var comments []model.Comment
	var total int64
	db := r.db.WithContext(ctx).Model(&model.Comment{}).Where("article_id = ?", articleID)
	if userID > 0 {
		db = db.Where("status = ? OR (status = ? AND user_id = ?)", constant.CommentStatusActive, constant.CommentStatusHidden, userID)
	} else {
		db = db.Where("status = ?", constant.CommentStatusActive)
	}
	db = db.Order("created_at ASC")
	err := db.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	err = db.Offset((page - 1) * pageSize).Limit(pageSize).Find(&comments).Error
	return comments, total, err
}

// ListReplies lists replies to comments authored by userID.
func (r *CommentRepo) ListReplies(ctx context.Context, userID int64, unreadOnly bool, page, pageSize int) ([]model.Comment, int64, error) {
	var userCommentIDs []int64
	err := r.db.WithContext(ctx).Model(&model.Comment{}).
		Where("user_id = ? AND status = ?", userID, constant.CommentStatusActive).
		Pluck("id", &userCommentIDs).Error
	if err != nil {
		return nil, 0, err
	}
	if len(userCommentIDs) == 0 {
		return []model.Comment{}, 0, nil
	}

	var replies []model.Comment
	var total int64
	db := r.db.WithContext(ctx).Model(&model.Comment{}).
		Where("parent_id IN ? AND status = ?", userCommentIDs, constant.CommentStatusActive)
	if unreadOnly {
		db = db.Where("is_read = ?", 0)
	}
	db = db.Order("created_at DESC")
	err = db.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	err = db.Offset((page - 1) * pageSize).Limit(pageSize).Find(&replies).Error
	return replies, total, err
}

// ListByUser lists comments authored by userID.
func (r *CommentRepo) ListByUser(ctx context.Context, userID int64, page, pageSize int) ([]model.Comment, int64, error) {
	var comments []model.Comment
	var total int64
	db := r.db.WithContext(ctx).Model(&model.Comment{}).Where("user_id = ?", userID).Order("created_at DESC")
	err := db.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	err = db.Offset((page - 1) * pageSize).Limit(pageSize).Find(&comments).Error
	return comments, total, err
}

// ListAll lists comments for admin with optional keyword and article id.
func (r *CommentRepo) ListAll(ctx context.Context, keyword string, articleID int64, page, pageSize int) ([]model.Comment, int64, error) {
	var comments []model.Comment
	var total int64
	db := r.db.WithContext(ctx).Model(&model.Comment{}).Order("created_at DESC")
	if keyword != "" {
		db = db.Where("content LIKE ?", "%"+keyword+"%")
	}
	if articleID > 0 {
		db = db.Where("article_id = ?", articleID)
	}
	err := db.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	err = db.Offset((page - 1) * pageSize).Limit(pageSize).Find(&comments).Error
	return comments, total, err
}
