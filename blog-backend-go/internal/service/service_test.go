package service

import (
	"context"
	"testing"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/crypto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/mail"
	appredis "github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/redis"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/go-redis/redis/v8"
	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
)

func setupTestDB(t *testing.T) *gorm.DB {
	db, err := gorm.Open(sqlite.Open("file::memory:?cache=shared"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open sqlite failed: %v", err)
	}
	if err := db.AutoMigrate(&model.User{}, &model.Article{}, &model.ArticleContent{}, &model.Comment{}, &model.Tag{}, &model.ArticleTag{}, &model.UserLike{}); err != nil {
		t.Fatalf("migrate failed: %v", err)
	}
	return db
}

func setupTestRedis(t *testing.T) *redis.Client {
	rdb := redis.NewClient(&redis.Options{Addr: "localhost:6379", DB: 15})
	if err := rdb.Ping(context.Background()).Err(); err != nil {
		t.Skipf("redis not available: %v", err)
	}
	return rdb
}

func TestUserServiceChangePassword(t *testing.T) {
	db := setupTestDB(t)
	repo := repository.NewUserRepo(db)
	svc := NewUserService(repo)

	hashed, _ := crypto.HashPassword("oldpass")
	user := &model.User{Username: "alice", Password: hashed, Role: constant.RoleUser, Status: constant.UserStatusActive, CreatedAt: time.Now(), UpdatedAt: time.Now()}
	if err := repo.Create(context.Background(), user); err != nil {
		t.Fatalf("create user failed: %v", err)
	}

	err := svc.ChangePassword(context.Background(), user.ID, &dto.ChangePasswordRequest{OldPassword: "oldpass", NewPassword: "newpass123"})
	if err != nil {
		t.Fatalf("change password failed: %v", err)
	}

	updated, _ := repo.GetByID(context.Background(), user.ID)
	if !crypto.CheckPassword("newpass123", updated.Password) {
		t.Fatal("password was not updated")
	}
}

func TestTagServiceCreateAndResolve(t *testing.T) {
	db := setupTestDB(t)
	tagRepo := repository.NewTagRepo(db)
	atRepo := repository.NewArticleTagRepo(db)
	svc := NewTagService(tagRepo, atRepo)

	tag, err := svc.CreateTag(context.Background(), "Go", 1)
	if err != nil {
		t.Fatalf("create tag failed: %v", err)
	}
	if tag.Name != "Go" {
		t.Fatalf("unexpected tag name: %s", tag.Name)
	}

	ids, err := svc.ResolveTagIDs(context.Background(), []string{"Go", "New"}, 1)
	if err != nil {
		t.Fatalf("resolve tags failed: %v", err)
	}
	if len(ids) != 2 {
		t.Fatalf("expected 2 tag ids, got %d", len(ids))
	}
}

// Ensure async service dependencies compile in tests.
func TestAsyncServiceInit(t *testing.T) {
	setupTestRedis(t) // validate redis availability
	appCfg := &config.AppEnvConfig{FrontendURL: "http://localhost:5173"}
	aiSvc := NewAIService(&config.AIConfig{Enabled: false})
	mailer := mail.NewSender(&config.EmailConfig{})
	rdb := appredis.New(&config.RedisConfig{Host: "localhost", Port: "6379", DB: 15})
	_ = NewAsyncService(appCfg, aiSvc, nil, rdb, mailer)
}
