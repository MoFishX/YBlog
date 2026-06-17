package service

import (
	"context"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/errs"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/redis"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
)

// AdminService handles admin business logic.
type AdminService struct {
	userRepo    *repository.UserRepo
	articleRepo *repository.ArticleRepo
	commentRepo *repository.CommentRepo
	rdb         *redis.Client
}

// NewAdminService creates an AdminService.
func NewAdminService(userRepo *repository.UserRepo, articleRepo *repository.ArticleRepo, commentRepo *repository.CommentRepo, rdb *redis.Client) *AdminService {
	return &AdminService{userRepo: userRepo, articleRepo: articleRepo, commentRepo: commentRepo, rdb: rdb}
}

// GetUserList returns paginated users for admin.
func (s *AdminService) GetUserList(ctx context.Context, page, pageSize int, keyword string) (*resp.PageResult[dto.UserVO], error) {
	page, pageSize = normalizePage(page, pageSize)
	users, total, err := s.userRepo.ListByKeyword(ctx, keyword, page, pageSize)
	if err != nil {
		return nil, err
	}
	vos := make([]dto.UserVO, 0, len(users))
	for _, u := range users {
		count, _ := s.userRepo.CountArticlesByUserID(ctx, u.ID)
		vos = append(vos, dto.UserVO{
			ID:           u.ID,
			Username:     u.Username,
			Email:        u.Email,
			Avatar:       u.Avatar,
			Role:         u.Role,
			ArticleCount: int(count),
			Status:       u.Status,
			CreatedAt:    u.CreatedAt,
		})
	}
	return &resp.PageResult[dto.UserVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

// UpdateUserStatus updates a user's status.
func (s *AdminService) UpdateUserStatus(ctx context.Context, userID int64, status string) error {
	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil {
		return err
	}
	if user == nil {
		return errs.New(constant.CodeNotFound, constant.MsgUserNotFound)
	}
	user.Status = status
	user.UpdatedAt = time.Now()
	return s.userRepo.Update(ctx, user)
}

// UpdateUserRole updates a user's role.
func (s *AdminService) UpdateUserRole(ctx context.Context, userID int64, role string) error {
	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil {
		return err
	}
	if user == nil {
		return errs.New(constant.CodeNotFound, constant.MsgUserNotFound)
	}
	user.Role = role
	user.UpdatedAt = time.Now()
	return s.userRepo.Update(ctx, user)
}

// GetStats returns dashboard statistics.
func (s *AdminService) GetStats(ctx context.Context) (*dto.StatsVO, error) {
	userCount, _ := s.userRepo.Count(ctx)
	articleCount, _ := s.articleRepo.CountAll(ctx)
	totalViews, _ := s.articleRepo.SumViewCount(ctx)
	totalLikes, _ := s.articleRepo.SumLikeCount(ctx)
	todayArticles, _ := s.articleRepo.CountToday(ctx)
	commentCount, _ := s.commentRepo.CountAll(ctx)

	today := time.Now().Format("2006-01-02")
	todayNewUsers, _ := s.rdb.GetInt64(ctx, "stats:new_users:"+today)
	todayViews, _ := s.rdb.GetInt64(ctx, "stats:views:"+today)

	return &dto.StatsVO{
		UserCount:        userCount,
		ArticleCount:     articleCount,
		CommentCount:     commentCount,
		TotalViews:       totalViews,
		TotalLikes:       totalLikes,
		TodayViews:       todayViews,
		TodayNewUsers:    todayNewUsers,
		TodayNewArticles: todayArticles,
	}, nil
}

// GetWeeklyTrend returns the last 7 days view trend.
func (s *AdminService) GetWeeklyTrend(ctx context.Context) ([]dto.TrendVO, error) {
	today := time.Now()
	trends := make([]dto.TrendVO, 0, 7)
	for i := 6; i >= 0; i-- {
		date := today.AddDate(0, 0, -i)
		dateKey := date.Format("2006-01-02")
		count, _ := s.rdb.GetInt64(ctx, "stats:views:"+dateKey)
		trends = append(trends, dto.TrendVO{
			Date:  date.Format("01-02"),
			Count: count,
		})
	}
	return trends, nil
}
