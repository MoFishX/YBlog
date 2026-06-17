package service

import (
	"context"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/errs"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/crypto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
)

// UserService handles user business logic.
type UserService struct {
	userRepo *repository.UserRepo
}

// NewUserService creates a UserService.
func NewUserService(userRepo *repository.UserRepo) *UserService {
	return &UserService{userRepo: userRepo}
}

// GetByID returns public user info.
func (s *UserService) GetByID(ctx context.Context, userID int64) (*dto.UserVO, error) {
	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, errs.New(constant.CodeNotFound, constant.MsgUserNotFound)
	}
	return s.toVO(ctx, user), nil
}

// UpdateProfile updates the current user's profile.
func (s *UserService) UpdateProfile(ctx context.Context, userID int64, req *dto.UpdateProfileRequest) (*dto.UserVO, error) {
	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, errs.New(constant.CodeNotFound, constant.MsgUserNotFound)
	}
	if req.Email != "" {
		user.Email = req.Email
	}
	if req.Avatar != "" {
		user.Avatar = req.Avatar
	}
	user.UpdatedAt = time.Now()
	if err := s.userRepo.Update(ctx, user); err != nil {
		return nil, err
	}
	return s.toVO(ctx, user), nil
}

// ChangePassword changes the current user's password.
func (s *UserService) ChangePassword(ctx context.Context, userID int64, req *dto.ChangePasswordRequest) error {
	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil {
		return err
	}
	if user == nil {
		return errs.New(constant.CodeNotFound, constant.MsgUserNotFound)
	}
	if !crypto.CheckPassword(req.OldPassword, user.Password) {
		return errs.New(constant.CodeBadRequest, constant.MsgWrongPassword)
	}
	hashed, err := crypto.HashPassword(req.NewPassword)
	if err != nil {
		return err
	}
	user.Password = hashed
	user.UpdatedAt = time.Now()
	return s.userRepo.Update(ctx, user)
}

func (s *UserService) toVO(ctx context.Context, user *model.User) *dto.UserVO {
	count, _ := s.userRepo.CountArticlesByUserID(ctx, user.ID)
	return &dto.UserVO{
		ID:           user.ID,
		Username:     user.Username,
		Email:        user.Email,
		Avatar:       user.Avatar,
		Role:         user.Role,
		ArticleCount: int(count),
		Status:       user.Status,
		CreatedAt:    user.CreatedAt,
	}
}
