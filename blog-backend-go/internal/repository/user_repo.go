package repository

import (
	"context"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"gorm.io/gorm"
)

// UserRepo handles user data access.
type UserRepo struct {
	db *gorm.DB
}

// NewUserRepo creates a UserRepo.
func NewUserRepo(db *gorm.DB) *UserRepo {
	return &UserRepo{db: db}
}

// Create inserts a new user.
func (r *UserRepo) Create(ctx context.Context, user *model.User) error {
	return r.db.WithContext(ctx).Create(user).Error
}

// GetByID retrieves a user by id.
func (r *UserRepo) GetByID(ctx context.Context, id int64) (*model.User, error) {
	var user model.User
	err := r.db.WithContext(ctx).First(&user, id).Error
	if err == gorm.ErrRecordNotFound {
		return nil, nil
	}
	return &user, err
}

// GetByUsername retrieves a user by username.
func (r *UserRepo) GetByUsername(ctx context.Context, username string) (*model.User, error) {
	var user model.User
	err := r.db.WithContext(ctx).Where("username = ?", username).First(&user).Error
	if err == gorm.ErrRecordNotFound {
		return nil, nil
	}
	return &user, err
}

// GetByEmail retrieves a user by email.
func (r *UserRepo) GetByEmail(ctx context.Context, email string) (*model.User, error) {
	var user model.User
	err := r.db.WithContext(ctx).Where("email = ?", email).First(&user).Error
	if err == gorm.ErrRecordNotFound {
		return nil, nil
	}
	return &user, err
}

// Update updates a user.
func (r *UserRepo) Update(ctx context.Context, user *model.User) error {
	return r.db.WithContext(ctx).Save(user).Error
}

// Count returns total user count.
func (r *UserRepo) Count(ctx context.Context) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.User{}).Count(&count).Error
	return count, err
}

// CountArticlesByUserID returns the number of articles authored by the user.
func (r *UserRepo) CountArticlesByUserID(ctx context.Context, userID int64) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.Article{}).Where("author_id = ?", userID).Count(&count).Error
	return count, err
}

// ListByKeyword paginates users filtered by username keyword.
func (r *UserRepo) ListByKeyword(ctx context.Context, keyword string, page, pageSize int) ([]model.User, int64, error) {
	var users []model.User
	var total int64
	db := r.db.WithContext(ctx).Model(&model.User{}).Order("created_at DESC")
	if keyword != "" {
		db = db.Where("username LIKE ?", "%"+keyword+"%")
	}
	err := db.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	err = db.Offset((page - 1) * pageSize).Limit(pageSize).Find(&users).Error
	return users, total, err
}
