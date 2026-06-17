package scheduler

import (
	"context"
	"log/slog"
	"strconv"
	"strings"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/redis"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/robfig/cron/v3"
)

// Scheduler wraps cron jobs.
type Scheduler struct {
	c           *cron.Cron
	articleRepo *repository.ArticleRepo
	rdb         *redis.Client
}

// NewScheduler creates a Scheduler.
func NewScheduler(articleRepo *repository.ArticleRepo, rdb *redis.Client) *Scheduler {
	return &Scheduler{c: cron.New(), articleRepo: articleRepo, rdb: rdb}
}

// Start registers and starts scheduled tasks.
func (s *Scheduler) Start(ctx context.Context) {
	_, _ = s.c.AddFunc("@every 5m", func() {
		s.syncViewCount(ctx)
	})
	s.c.Start()
}

// Stop gracefully stops the scheduler.
func (s *Scheduler) Stop() {
	ctx := s.c.Stop()
	select {
	case <-ctx.Done():
	case <-time.After(5 * time.Second):
	}
}

func (s *Scheduler) syncViewCount(ctx context.Context) {
	keys, err := s.rdb.Keys(ctx, "article:view:*")
	if err != nil || len(keys) == 0 {
		return
	}
	for _, key := range keys {
		idStr := key[strings.LastIndex(key, ":")+1:]
		articleID, err := strconv.ParseInt(idStr, 10, 64)
		if err != nil {
			continue
		}
		incr, err := s.rdb.GetSet(ctx, key, "0")
		if err != nil {
			continue
		}
		if incr > 0 {
			if err := s.articleRepo.IncrementViewCount(ctx, articleID, incr); err != nil {
				slog.Error("sync view count failed", slog.String("key", key), slog.String("error", err.Error()))
			}
		}
	}
	slog.Info("view count sync completed", slog.Int("articles", len(keys)))
}
