package main

import (
	"context"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/handler"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/handler/admin"
	pubh "github.com/MoFishX/YBlog/blog-backend-go/internal/handler/pub"
	userh "github.com/MoFishX/YBlog/blog-backend-go/internal/handler/user"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/crypto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/db"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/jwt"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/mail"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/redis"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/storage"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/scheduler"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

func main() {
	cfg := config.Load()

	// Setup structured logging.
	slog.SetDefault(slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{Level: slog.LevelInfo})))

	// Database.
	gormDB, err := db.New(&cfg.MySQL)
	if err != nil {
		slog.Error("failed to connect to mysql", slog.String("error", err.Error()))
		os.Exit(1)
	}
	if err := db.AutoMigrate(gormDB); err != nil {
		slog.Error("failed to auto migrate", slog.String("error", err.Error()))
		os.Exit(1)
	}

	// Redis.
	rdb := redis.New(&cfg.Redis)
	if err := rdb.Ping(context.Background()); err != nil {
		slog.Error("failed to connect to redis", slog.String("error", err.Error()))
		os.Exit(1)
	}

	// JWT.
	jwtMgr, err := jwt.NewManager(&cfg.JWT)
	if err != nil {
		slog.Error("failed to init jwt", slog.String("error", err.Error()))
		os.Exit(1)
	}

	// Repositories.
	userRepo := repository.NewUserRepo(gormDB)
	articleRepo := repository.NewArticleRepo(gormDB)
	articleContentRepo := repository.NewArticleContentRepo(gormDB)
	commentRepo := repository.NewCommentRepo(gormDB)
	tagRepo := repository.NewTagRepo(gormDB)
	articleTagRepo := repository.NewArticleTagRepo(gormDB)
	userLikeRepo := repository.NewUserLikeRepo(gormDB)

	// Infrastructure services.
	aiSvc := service.NewAIService(&cfg.AI)
	mailer := mail.NewSender(&cfg.Email)
	uploader := storage.NewUploader(&cfg.Storage)
	asyncSvc := service.NewAsyncService(&cfg.App, aiSvc, articleRepo, rdb, mailer)

	// Business services.
	tagSvc := service.NewTagService(tagRepo, articleTagRepo)
	userSvc := service.NewUserService(userRepo)
	articleSvc := service.NewArticleService(articleRepo, articleContentRepo, tagRepo, userRepo, commentRepo, articleTagRepo, userLikeRepo, rdb, asyncSvc, tagSvc)
	commentSvc := service.NewCommentService(commentRepo, articleRepo, userRepo)
	adminSvc := service.NewAdminService(userRepo, articleRepo, commentRepo, rdb)
	authSvc := service.NewAuthService(cfg, userRepo, jwtMgr, rdb, asyncSvc)

	// Admin initializer.
	initAdmin(context.Background(), cfg, userRepo)

	// Scheduler.
	sched := scheduler.NewScheduler(articleRepo, rdb)
	sched.Start(context.Background())

	// HTTP server.
	gin.SetMode(gin.ReleaseMode)
	r := gin.New()
	r.Use(middleware.Recovery())
	r.Use(middleware.Logger())
	r.Use(middleware.CORS(&cfg.App))
	r.Use(middleware.ErrorHandler())
	r.Use(middleware.Auth(jwtMgr, rdb, userRepo))

	// Static uploads.
	r.GET("/uploads/*filepath", handler.StaticHandler(uploader))

	// Handlers.
	api := r.Group("")
	pubh.NewArticleHandler(articleSvc).Register(api)
	pubh.NewAuthHandler(authSvc).RegisterRoutes(api)
	pubh.NewCommentHandler(commentSvc).Register(api)
	pubh.NewTagHandler(tagSvc).Register(api)

	userh.NewArticleHandler(articleSvc).Register(api)
	userh.NewCommentHandler(commentSvc).Register(api)
	userh.NewUserHandler(userSvc).Register(api)

	handler.NewUploadHandler(uploader).Register(api)

	admin.NewHandler(adminSvc, articleSvc, commentSvc, tagSvc).Register(api)

	srv := &http.Server{
		Addr:    ":" + cfg.Server.Port,
		Handler: r,
	}

	go func() {
		slog.Info("server started", slog.String("addr", srv.Addr))
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			slog.Error("server error", slog.String("error", err.Error()))
			os.Exit(1)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	slog.Info("shutting down server")
	shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	_ = srv.Shutdown(shutdownCtx)
	_ = asyncSvc.Shutdown(shutdownCtx)
	sched.Stop()
	_ = rdb.Close()
}

func initAdmin(ctx context.Context, cfg *config.AppConfig, userRepo *repository.UserRepo) {
	if cfg.Admin.Username == "" || cfg.Admin.Password == "" {
		slog.Info("admin credentials not set, skipping admin init")
		return
	}
	existing, err := userRepo.GetByUsername(ctx, cfg.Admin.Username)
	if err != nil {
		slog.Error("failed to check admin user", slog.String("error", err.Error()))
		return
	}
	if existing != nil {
		slog.Info("admin already exists", slog.String("username", cfg.Admin.Username))
		return
	}
	hashed, err := crypto.HashPassword(cfg.Admin.Password)
	if err != nil {
		slog.Error("failed to hash admin password", slog.String("error", err.Error()))
		return
	}
	now := time.Now()
	admin := &model.User{
		Username:  cfg.Admin.Username,
		Password:  hashed,
		Email:     cfg.Admin.Email,
		Role:      "ADMIN",
		Status:    "ACTIVE",
		CreatedAt: now,
		UpdatedAt: now,
	}
	if err := userRepo.Create(ctx, admin); err != nil {
		slog.Error("failed to create admin", slog.String("error", err.Error()))
		return
	}
	slog.Info("admin created", slog.String("username", cfg.Admin.Username))
}
