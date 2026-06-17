package service

import (
	"context"
	"fmt"
	"log/slog"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/errs"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/crypto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/jwt"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/redis"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
)

// AuthService handles authentication business logic.
type AuthService struct {
	cfg         *config.AppConfig
	userRepo    *repository.UserRepo
	jwtMgr      *jwt.Manager
	rdb         *redis.Client
	asyncSvc    *AsyncService
}

// NewAuthService creates an AuthService.
func NewAuthService(cfg *config.AppConfig, userRepo *repository.UserRepo, jwtMgr *jwt.Manager, rdb *redis.Client, asyncSvc *AsyncService) *AuthService {
	return &AuthService{cfg: cfg, userRepo: userRepo, jwtMgr: jwtMgr, rdb: rdb, asyncSvc: asyncSvc}
}

// Register creates a new user account.
func (s *AuthService) Register(ctx context.Context, req *dto.RegisterRequest) error {
	existing, err := s.userRepo.GetByUsername(ctx, req.Username)
	if err != nil {
		return err
	}
	if existing != nil {
		return errs.New(constant.CodeUsernameExists, constant.MsgUsernameExists)
	}

	hashed, err := crypto.HashPassword(req.Password)
	if err != nil {
		return err
	}

	now := time.Now()
	user := &model.User{
		Username:  req.Username,
		Password:  hashed,
		Email:     req.Email,
		Role:      constant.RoleUser,
		Status:    constant.UserStatusInactive,
		CreatedAt: now,
		UpdatedAt: now,
	}
	if err := s.userRepo.Create(ctx, user); err != nil {
		return err
	}

	// Stats: new user count.
	today := now.Format("2006-01-02")
	_, _ = s.rdb.Increment(ctx, "stats:new_users:"+today)

	if req.Email != "" {
		s.asyncSvc.SendActivationEmail(ctx, user.ID, req.Email)
	}
	return nil
}

// Login validates credentials and issues tokens.
func (s *AuthService) Login(ctx context.Context, req *dto.LoginRequest) (*dto.LoginResult, error) {
	user, err := s.userRepo.GetByUsername(ctx, req.Username)
	if err != nil {
		return nil, err
	}
	if user == nil || !crypto.CheckPassword(req.Password, user.Password) {
		return nil, errs.New(constant.CodeUnauthorized, constant.MsgWrongUserOrPassword)
	}
	if user.Status == constant.UserStatusBanned {
		return nil, errs.New(constant.CodeForbidden, constant.MsgUserBanned)
	}

	accessToken, err := s.jwtMgr.GenerateAccessToken(user.ID, user.Username, user.Role)
	if err != nil {
		return nil, err
	}
	refreshToken, err := s.jwtMgr.GenerateRefreshToken(user.ID)
	if err != nil {
		return nil, err
	}

	return &dto.LoginResult{
		AccessToken:  accessToken,
		RefreshToken: refreshToken,
		ExpiresIn:    s.jwtMgr.AccessExpirationSeconds(),
		User: dto.LoginVO{
			ID:       user.ID,
			Username: user.Username,
			Email:    user.Email,
			Avatar:   user.Avatar,
			Role:     user.Role,
			Status:   user.Status,
		},
	}, nil
}

// RefreshToken validates the refresh token and issues a new access token.
func (s *AuthService) RefreshToken(ctx context.Context, refreshToken string) (*dto.RefreshResult, error) {
	userID, err := s.jwtMgr.ValidateRefresh(refreshToken)
	if err != nil {
		return nil, errs.New(constant.CodeUnauthorized, constant.MsgUnauthorized)
	}

	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, errs.New(constant.CodeUnauthorized, constant.MsgUnauthorized)
	}
	if user.Status == constant.UserStatusBanned {
		return nil, errs.New(constant.CodeForbidden, constant.MsgUserBanned)
	}

	accessToken, err := s.jwtMgr.GenerateAccessToken(user.ID, user.Username, user.Role)
	if err != nil {
		return nil, err
	}

	return &dto.RefreshResult{
		AccessToken: accessToken,
		ExpiresIn:   s.jwtMgr.AccessExpirationSeconds(),
		User: dto.LoginVO{
			ID:       user.ID,
			Username: user.Username,
			Email:    user.Email,
			Avatar:   user.Avatar,
			Role:     user.Role,
			Status:   user.Status,
		},
	}, nil
}

// Logout blacklists the access token.
func (s *AuthService) Logout(ctx context.Context, token string) error {
	_, err := s.jwtMgr.Validate(token)
	if err != nil {
		return nil
	}
	return s.rdb.SetEX(ctx, "blacklist:token:"+token, "1", time.Duration(s.jwtMgr.AccessExpirationSeconds())*time.Second)
}

// VerifyEmail activates a user account using the verification token.
func (s *AuthService) VerifyEmail(ctx context.Context, token string) error {
	key := "email:verify:" + token
	userIDStr, err := s.rdb.GetString(ctx, key)
	if err != nil {
		return errs.New(constant.CodeUnauthorized, constant.MsgVerifyLinkInvalid)
	}

	var userID int64
	if _, err := fmt.Sscanf(userIDStr, "%d", &userID); err != nil {
		return errs.New(constant.CodeUnauthorized, constant.MsgVerifyLinkInvalid)
	}

	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil {
		return err
	}
	if user == nil {
		return errs.New(constant.CodeNotFound, constant.MsgUserNotFound)
	}
	if user.Status == constant.UserStatusActive {
		_ = s.rdb.Delete(ctx, key)
		return nil
	}

	user.Status = constant.UserStatusActive
	user.UpdatedAt = time.Now()
	if err := s.userRepo.Update(ctx, user); err != nil {
		return err
	}
	_ = s.rdb.Delete(ctx, key)
	slog.Info("email verified", slog.Int64("userId", userID))
	return nil
}

// ResendActivation resends the activation email.
func (s *AuthService) ResendActivation(ctx context.Context, email string) error {
	user, err := s.userRepo.GetByEmail(ctx, email)
	if err != nil {
		return err
	}
	if user == nil {
		return errs.New(constant.CodeNotFound, constant.MsgUserNotFound)
	}
	if user.Status == constant.UserStatusActive {
		return errs.New(constant.CodeForbidden, constant.MsgEmailAlreadyActivated)
	}
	s.asyncSvc.SendActivationEmail(ctx, user.ID, email)
	return nil
}
