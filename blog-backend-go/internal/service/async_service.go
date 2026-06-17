package service

import (
	"context"
	"fmt"
	"log/slog"
	"sync"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/mail"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/redis"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/google/uuid"
)

// AsyncService handles background tasks.
type AsyncService struct {
	cfg      *config.AppEnvConfig
	aiSvc    *AIService
	articleRepo *repository.ArticleRepo
	rdb      *redis.Client
	mailer   *mail.Sender
	wg       sync.WaitGroup
}

// NewAsyncService creates an AsyncService.
func NewAsyncService(cfg *config.AppEnvConfig, aiSvc *AIService, articleRepo *repository.ArticleRepo, rdb *redis.Client, mailer *mail.Sender) *AsyncService {
	return &AsyncService{cfg: cfg, aiSvc: aiSvc, articleRepo: articleRepo, rdb: rdb, mailer: mailer}
}

// Shutdown waits for running background tasks.
func (s *AsyncService) Shutdown(ctx context.Context) error {
	done := make(chan struct{})
	go func() {
		s.wg.Wait()
		close(done)
	}()
	select {
	case <-done:
		return nil
	case <-ctx.Done():
		return ctx.Err()
	}
}

// GenerateArticleSummary generates a short AI summary in the background.
func (s *AsyncService) GenerateArticleSummary(ctx context.Context, articleID int64, title, content string) {
	s.wg.Add(1)
	go func() {
		defer s.wg.Done()
		s.generateArticleAi(context.Background(), articleID, title, content, "SUMMARY")
	}()
}

// GenerateArticleSummaryLong generates a long AI summary in the background.
func (s *AsyncService) GenerateArticleSummaryLong(ctx context.Context, articleID int64, title, content string) {
	s.wg.Add(1)
	go func() {
		defer s.wg.Done()
		s.generateArticleAi(context.Background(), articleID, title, content, "SUMMARY_LONG")
	}()
}

func (s *AsyncService) generateArticleAi(ctx context.Context, articleID int64, title, content, typ string) {
	lockKey := "ai:generate:lock:" + typ + ":" + fmt.Sprintf("%d", articleID)
	exists, err := s.rdb.Exists(ctx, lockKey)
	if err == nil && exists {
		slog.Warn("ai generation locked", slog.Int64("articleId", articleID), slog.String("type", typ))
		return
	}
	_ = s.rdb.SetEX(ctx, lockKey, "1", 10*time.Minute)
	defer s.rdb.Delete(ctx, lockKey)

	if content == "" {
		slog.Warn("article content empty, skipping ai", slog.Int64("articleId", articleID))
		return
	}

	article, err := s.articleRepo.GetByID(ctx, articleID)
	if err != nil || article == nil {
		slog.Warn("article not found, skipping ai", slog.Int64("articleId", articleID))
		return
	}

	var result string
	switch typ {
	case "SUMMARY":
		result = s.aiSvc.Summarize(title, content)
		article.AiSummary = result
	case "SUMMARY_LONG":
		result = s.aiSvc.SummarizeLong(title, content)
		article.AiSummaryLong = result
	}

	if result != "" {
		article.UpdatedAt = time.Now()
		if err := s.articleRepo.Update(ctx, article); err != nil {
			slog.Error("failed to save ai summary", slog.Int64("articleId", articleID), slog.String("error", err.Error()))
			return
		}
		slog.Info("ai summary generated", slog.Int64("articleId", articleID), slog.String("type", typ))
	}
}

// SendActivationEmail sends the activation email asynchronously.
func (s *AsyncService) SendActivationEmail(ctx context.Context, userID int64, email string) {
	s.wg.Add(1)
	go func() {
		defer s.wg.Done()
		token := uuid.New().String()
		_ = s.rdb.SetEX(context.Background(), "email:verify:"+token, fmt.Sprintf("%d", userID), 24*time.Hour)

		verifyURL := s.cfg.FrontendURL + "/verify-email?token=" + token
		s.mailer.SendHTML("Blog <noreply@mail.yvmoux.com>", email, "激活你的博客账号", mail.BuildActivationEmail(verifyURL))
		slog.Info("activation email sent", slog.String("email", email), slog.Int64("userId", userID))
	}()
}

// AsyncService intentionally does not depend on UserRepo to avoid circular imports.
// User status checks are performed by callers before invoking background tasks.
