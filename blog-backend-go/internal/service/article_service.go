package service

import (
	"context"
	"fmt"
	"regexp"
	"strconv"
	"strings"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/errs"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/redis"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
)

// ArticleService handles article business logic.
type ArticleService struct {
	articleRepo       *repository.ArticleRepo
	articleContentRepo *repository.ArticleContentRepo
	tagRepo           *repository.TagRepo
	userRepo          *repository.UserRepo
	commentRepo       *repository.CommentRepo
	articleTagRepo    *repository.ArticleTagRepo
	userLikeRepo      *repository.UserLikeRepo
	rdb               *redis.Client
	asyncSvc          *AsyncService
	tagSvc            *TagService
}

// NewArticleService creates an ArticleService.
func NewArticleService(
	articleRepo *repository.ArticleRepo,
	articleContentRepo *repository.ArticleContentRepo,
	tagRepo *repository.TagRepo,
	userRepo *repository.UserRepo,
	commentRepo *repository.CommentRepo,
	articleTagRepo *repository.ArticleTagRepo,
	userLikeRepo *repository.UserLikeRepo,
	rdb *redis.Client,
	asyncSvc *AsyncService,
	tagSvc *TagService,
) *ArticleService {
	return &ArticleService{
		articleRepo:        articleRepo,
		articleContentRepo: articleContentRepo,
		tagRepo:            tagRepo,
		userRepo:           userRepo,
		commentRepo:        commentRepo,
		articleTagRepo:     articleTagRepo,
		userLikeRepo:       userLikeRepo,
		rdb:                rdb,
		asyncSvc:           asyncSvc,
		tagSvc:             tagSvc,
	}
}

// GetArticleList returns a paginated article list.
func (s *ArticleService) GetArticleList(ctx context.Context, q *dto.ArticleQuery) (*resp.PageResult[dto.ArticleVO], error) {
	page, pageSize := normalizePage(q.Page, q.PageSize)
	var articles []model.Article
	var total int64
	var err error

	if q.TagName != "" {
		articles, total, err = s.articleRepo.ListByTagName(ctx, q.TagName, q.Status, q.OrderBy, page, pageSize)
	} else {
		articles, total, err = s.articleRepo.List(ctx, q.Status, q.AuthorID, q.OrderBy, page, pageSize)
	}
	if err != nil {
		return nil, err
	}
	vos := make([]dto.ArticleVO, 0, len(articles))
	for _, a := range articles {
		vos = append(vos, s.toListVO(ctx, &a))
	}
	return &resp.PageResult[dto.ArticleVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

// GetArticleDetail returns article detail and records view stats.
func (s *ArticleService) GetArticleDetail(ctx context.Context, articleID, currentUserID int64, isAdmin bool) (dto.ArticleVO, error) {
	article, err := s.articleRepo.GetByID(ctx, articleID)
	if err != nil {
		return dto.ArticleVO{}, err
	}
	if article == nil {
		return dto.ArticleVO{}, errs.New(constant.CodeNotFound, constant.MsgArticleNotFound)
	}
	isAuthor := currentUserID > 0 && article.AuthorID == currentUserID
	if !isAuthor && !isAdmin && article.Status == constant.ArticleStatusDraft {
		return dto.ArticleVO{}, errs.New(constant.CodeNotFound, constant.MsgNotFound)
	}

	// Stats
	today := time.Now().Format("2006-01-02")
	_, _ = s.rdb.Increment(ctx, "article:view:"+fmt.Sprintf("%d", articleID))
	_, _ = s.rdb.ZIncrBy(ctx, "hot:articles", 1, fmt.Sprintf("%d", articleID))
	_, _ = s.rdb.Increment(ctx, "stats:views:"+today)
	_ = s.rdb.Expire(ctx, "stats:views:"+today, 8*24*time.Hour)
	if currentUserID > 0 {
		_ = s.rdb.SAdd(ctx, "stats:visitors:"+today, fmt.Sprintf("%d", currentUserID))
		_ = s.rdb.Expire(ctx, "stats:visitors:"+today, 8*24*time.Hour)
	}

	content, _ := s.articleContentRepo.GetByArticleID(ctx, articleID)
	vo := s.toDetailVO(ctx, article, content)
	if currentUserID > 0 {
		count, _ := s.userLikeRepo.CountByUserAndArticle(ctx, currentUserID, articleID)
		vo.IsLiked = count > 0
	}
	return vo, nil
}

// CreateArticle creates a new article.
func (s *ArticleService) CreateArticle(ctx context.Context, userID int64, req *dto.ArticleCreateRequest) (dto.ArticleVO, error) {
	status := strings.ToUpper(req.Status)
	if status == "" || (status != constant.ArticleStatusDraft && status != constant.ArticleStatusPublished) {
		status = constant.ArticleStatusDraft
	}

	content := req.Content
	if strings.TrimSpace(content) == "" {
		content = "似乎什么也没写呢~"
	}

	summary := req.Summary
	if strings.TrimSpace(summary) == "" {
		summary = extractPlainText(req.Content)
		if content == "似乎什么也没写呢~" {
			summary = "似乎什么也没写呢~"
		}
	}

	now := time.Now()
	article := &model.Article{
		Title:      req.Title,
		Summary:    summary,
		CoverImage: req.CoverImage,
		AuthorID:   userID,
		Status:     status,
		ViewCount:  0,
		LikeCount:  0,
		CreatedAt:  now,
		UpdatedAt:  now,
	}

	err := s.articleRepo.Create(ctx, article)
	if err != nil {
		return dto.ArticleVO{}, err
	}

	if err := s.articleContentRepo.Create(ctx, &model.ArticleContent{ArticleID: article.ID, Content: content}); err != nil {
		return dto.ArticleVO{}, err
	}

	tagIDs, err := s.resolveTagIDs(ctx, req.TagIDs, req.TagNames, userID)
	if err != nil {
		return dto.ArticleVO{}, err
	}
	if len(tagIDs) > 0 {
		if err := s.articleTagRepo.InsertBatch(ctx, article.ID, tagIDs); err != nil {
			return dto.ArticleVO{}, err
		}
	}

	if status == constant.ArticleStatusPublished {
		if req.GenAiSummary == 1 {
			s.asyncSvc.GenerateArticleSummary(ctx, article.ID, article.Title, content)
		}
		if req.GenAiSummaryLong == 1 {
			s.asyncSvc.GenerateArticleSummaryLong(ctx, article.ID, article.Title, content)
		}
	}

	return s.toDetailVO(ctx, article, &model.ArticleContent{ArticleID: article.ID, Content: content}), nil
}

// UpdateArticle updates an existing article.
func (s *ArticleService) UpdateArticle(ctx context.Context, articleID, userID int64, isAdmin bool, req *dto.ArticleUpdateRequest) (dto.ArticleVO, error) {
	article, err := s.articleRepo.GetByID(ctx, articleID)
	if err != nil {
		return dto.ArticleVO{}, err
	}
	if article == nil {
		return dto.ArticleVO{}, errs.New(constant.CodeNotFound, constant.MsgArticleNotFound)
	}
	if article.AuthorID != userID && !isAdmin {
		return dto.ArticleVO{}, errs.New(constant.CodeForbidden, constant.MsgForbidden)
	}

	if req.Title != "" {
		article.Title = req.Title
	}
	if req.Summary != "" {
		article.Summary = req.Summary
	}
	if req.CoverImage != "" {
		article.CoverImage = req.CoverImage
	}
	if req.Status != "" {
		status := strings.ToUpper(req.Status)
		if status == constant.ArticleStatusDraft || status == constant.ArticleStatusPublished {
			article.Status = status
		}
	}
	article.UpdatedAt = time.Now()
	if err := s.articleRepo.Update(ctx, article); err != nil {
		return dto.ArticleVO{}, err
	}

	articleContent := req.Content
	if articleContent != "" {
		existing, _ := s.articleContentRepo.GetByArticleID(ctx, articleID)
		if existing != nil {
			existing.Content = articleContent
			if err := s.articleContentRepo.Update(ctx, existing); err != nil {
				return dto.ArticleVO{}, err
			}
		} else {
			if err := s.articleContentRepo.Create(ctx, &model.ArticleContent{ArticleID: articleID, Content: articleContent}); err != nil {
				return dto.ArticleVO{}, err
			}
		}
	}

	tagIDs, err := s.resolveTagIDs(ctx, req.TagIDs, req.TagNames, userID)
	if err != nil {
		return dto.ArticleVO{}, err
	}
	if len(tagIDs) > 0 {
		if err := s.articleTagRepo.DeleteByArticleID(ctx, articleID); err != nil {
			return dto.ArticleVO{}, err
		}
		if err := s.articleTagRepo.InsertBatch(ctx, articleID, tagIDs); err != nil {
			return dto.ArticleVO{}, err
		}
	}

	contentForAI := articleContent
	if contentForAI == "" {
		existing, _ := s.articleContentRepo.GetByArticleID(ctx, articleID)
		if existing != nil {
			contentForAI = existing.Content
		}
	}
	if article.Status == constant.ArticleStatusPublished {
		if req.GenAiSummary == 1 {
			s.asyncSvc.GenerateArticleSummary(ctx, article.ID, article.Title, contentForAI)
		}
		if req.GenAiSummaryLong == 1 {
			s.asyncSvc.GenerateArticleSummaryLong(ctx, article.ID, article.Title, contentForAI)
		}
	}

	content, _ := s.articleContentRepo.GetByArticleID(ctx, articleID)
	return s.toDetailVO(ctx, article, content), nil
}

// DeleteArticle deletes articles by comma-separated ids.
func (s *ArticleService) DeleteArticle(ctx context.Context, idsStr string, userID int64, isAdmin bool) error {
	ids, err := parseIDs(idsStr)
	if err != nil {
		return errs.New(constant.CodeBadRequest, constant.MsgBadRequest)
	}
	if len(ids) == 0 {
		return errs.New(constant.CodeBadRequest, constant.MsgBadRequest)
	}

	articles := make([]*model.Article, 0, len(ids))
	for _, id := range ids {
		article, err := s.articleRepo.GetByID(ctx, id)
		if err != nil {
			return err
		}
		if article == nil {
			return errs.New(constant.CodeNotFound, constant.MsgArticleNotFound)
		}
		if article.AuthorID != userID && !isAdmin {
			return errs.New(constant.CodeForbidden, constant.MsgForbidden)
		}
		articles = append(articles, article)
	}

	// Delete in order.
	for _, id := range ids {
		_ = s.articleTagRepo.DeleteByArticleID(ctx, id)
		_ = s.userLikeRepo.DeleteByArticleIDs(ctx, []int64{id})
		_ = s.deleteCommentsByArticleID(ctx, id)
		_ = s.articleContentRepo.DeleteByArticleIDs(ctx, []int64{id})
		_ = s.articleRepo.DeleteByIDs(ctx, []int64{id})
	}
	return nil
}

// DeleteArticleOne deletes a single article.
func (s *ArticleService) DeleteArticleOne(ctx context.Context, articleID, userID int64) error {
	return s.DeleteArticle(ctx, fmt.Sprintf("%d", articleID), userID, false)
}

// ToggleLike toggles like status for an article.
func (s *ArticleService) ToggleLike(ctx context.Context, articleID, userID int64) (dto.ArticleVO, error) {
	article, err := s.articleRepo.GetByID(ctx, articleID)
	if err != nil {
		return dto.ArticleVO{}, err
	}
	if article == nil {
		return dto.ArticleVO{}, errs.New(constant.CodeNotFound, constant.MsgArticleNotFound)
	}

	count, err := s.userLikeRepo.CountByUserAndArticle(ctx, userID, articleID)
	if err != nil {
		return dto.ArticleVO{}, err
	}
	liked := count > 0
	if liked {
		if err := s.userLikeRepo.Delete(ctx, userID, articleID); err != nil {
			return dto.ArticleVO{}, err
		}
		_ = s.articleRepo.IncrementLikeCount(ctx, articleID, -1)
		_, _ = s.rdb.ZIncrBy(ctx, "hot:articles", -2, fmt.Sprintf("%d", articleID))
	} else {
		if err := s.userLikeRepo.Create(ctx, &model.UserLike{UserID: userID, ArticleID: articleID, CreatedAt: time.Now()}); err != nil {
			return dto.ArticleVO{}, err
		}
		_ = s.articleRepo.IncrementLikeCount(ctx, articleID, 1)
		_, _ = s.rdb.ZIncrBy(ctx, "hot:articles", 2, fmt.Sprintf("%d", articleID))
	}

	article, _ = s.articleRepo.GetByID(ctx, articleID)
	content, _ := s.articleContentRepo.GetByArticleID(ctx, articleID)
	vo := s.toDetailVO(ctx, article, content)
	vo.IsLiked = !liked
	return vo, nil
}

// GetHotArticles returns the hottest articles from Redis ZSet.
func (s *ArticleService) GetHotArticles(ctx context.Context, limit int) ([]dto.ArticleVO, error) {
	if limit > 50 {
		limit = 50
	}
	members, err := s.rdb.ZRevRange(ctx, "hot:articles", 0, int64(limit-1))
	if err != nil {
		return nil, err
	}
	vos := make([]dto.ArticleVO, 0, len(members))
	for i := len(members) - 1; i >= 0; i-- {
		id, err := strconv.ParseInt(members[i], 10, 64)
		if err != nil {
			continue
		}
		article, err := s.articleRepo.GetByID(ctx, id)
		if err != nil || article == nil {
			continue
		}
		vos = append(vos, s.toListVO(ctx, article))
	}
	return vos, nil
}

// GetAllArticles returns paginated articles for admin.
func (s *ArticleService) GetAllArticles(ctx context.Context, page, pageSize int, status, keyword string) (*resp.PageResult[dto.ArticleVO], error) {
	page, pageSize = normalizePage(page, pageSize)
	articles, total, err := s.articleRepo.ListAll(ctx, status, keyword, page, pageSize)
	if err != nil {
		return nil, err
	}
	vos := make([]dto.ArticleVO, 0, len(articles))
	for _, a := range articles {
		vos = append(vos, s.toListVO(ctx, &a))
	}
	return &resp.PageResult[dto.ArticleVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

// Search searches published articles by keyword.
func (s *ArticleService) Search(ctx context.Context, keyword string, page, pageSize int) (*resp.PageResult[dto.ArticleVO], error) {
	page, pageSize = normalizePage(page, pageSize)
	offset := (page - 1) * pageSize
	articles, total, err := s.articleRepo.Search(ctx, keyword, offset, pageSize)
	if err != nil {
		return nil, err
	}
	vos := make([]dto.ArticleVO, 0, len(articles))
	for _, a := range articles {
		vos = append(vos, s.toListVO(ctx, &a))
	}
	return &resp.PageResult[dto.ArticleVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

// TriggerAiSummaryLong triggers long AI summary generation.
func (s *ArticleService) TriggerAiSummaryLong(ctx context.Context, userID, articleID int64, isAdmin bool) error {
	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil {
		return err
	}
	if user == nil {
		return errs.New(constant.CodeNotFound, constant.MsgUserNotFound)
	}
	article, err := s.articleRepo.GetByID(ctx, articleID)
	if err != nil {
		return err
	}
	if article == nil {
		return errs.New(constant.CodeNotFound, constant.MsgArticleNotFound)
	}
	if !isAdmin && article.AiSummaryLong != "" {
		return errs.New(constant.CodeForbidden, constant.MsgForbidden)
	}
	content, _ := s.articleContentRepo.GetByArticleID(ctx, articleID)
	contentText := ""
	if content != nil {
		contentText = content.Content
	}
	s.asyncSvc.GenerateArticleSummaryLong(ctx, article.ID, article.Title, contentText)
	return nil
}

// GetAiSummaryLong returns the long AI summary status and content.
func (s *ArticleService) GetAiSummaryLong(ctx context.Context, articleID int64) (*dto.AiSummaryLongVO, error) {
	article, err := s.articleRepo.GetByID(ctx, articleID)
	if err != nil {
		return nil, err
	}
	if article == nil {
		return nil, errs.New(constant.CodeNotFound, constant.MsgArticleNotFound)
	}
	status := 1
	if article.AiSummaryLong == "" {
		status = 0
	}
	return &dto.AiSummaryLongVO{Status: status, SummaryLong: article.AiSummaryLong}, nil
}

// DeleteAiSummary clears the AI summary fields.
func (s *ArticleService) DeleteAiSummary(ctx context.Context, articleID int64) error {
	article, err := s.articleRepo.GetByID(ctx, articleID)
	if err != nil {
		return err
	}
	if article == nil {
		return errs.New(constant.CodeNotFound, constant.MsgArticleNotFound)
	}
	article.AiSummary = ""
	article.UpdatedAt = time.Now()
	return s.articleRepo.Update(ctx, article)
}

func (s *ArticleService) toListVO(ctx context.Context, article *model.Article) dto.ArticleVO {
	author, _ := s.userRepo.GetByID(ctx, article.AuthorID)
	tags, _ := s.tagRepo.ListByArticleID(ctx, article.ID)
	count, _ := s.commentRepo.CountByArticleID(ctx, article.ID)
	vo := dto.ArticleVO{
		ID:                  article.ID,
		Title:               article.Title,
		Summary:             article.Summary,
		CoverImage:          article.CoverImage,
		Status:              article.Status,
		Tags:                toTagVOs(tags),
		ViewCount:           article.ViewCount,
		LikeCount:           article.LikeCount,
		CommentCount:        int(count),
		IsLiked:             false,
		CreatedAt:           article.CreatedAt,
		UpdatedAt:           article.UpdatedAt,
		AiSummary:           article.AiSummary,
		AiSummaryStatus:     aiStatus(article.AiSummary),
		AiSummaryLong:       article.AiSummaryLong,
		AiSummaryLongStatus: aiStatus(article.AiSummaryLong),
	}
	if author != nil {
		vo.Author = &dto.AuthorVO{ID: author.ID, Username: author.Username, Avatar: author.Avatar}
	}
	return vo
}

func (s *ArticleService) toDetailVO(ctx context.Context, article *model.Article, content *model.ArticleContent) dto.ArticleVO {
	vo := s.toListVO(ctx, article)
	if content != nil {
		vo.Content = content.Content
	}
	return vo
}

func (s *ArticleService) resolveTagIDs(ctx context.Context, tagIDs []int64, tagNames []string, userID int64) ([]int64, error) {
	if len(tagNames) > 0 {
		return s.tagSvc.ResolveTagIDs(ctx, tagNames, userID)
	}
	if len(tagIDs) > 0 {
		return tagIDs, nil
	}
	return []int64{}, nil
}

func (s *ArticleService) deleteCommentsByArticleID(ctx context.Context, articleID int64) error {
	return s.commentRepo.DeleteByArticleID(ctx, articleID)
}

func normalizePage(page, pageSize int) (int, int) {
	if page <= 0 {
		page = 1
	}
	if pageSize <= 0 {
		pageSize = 10
	}
	if pageSize > 100 {
		pageSize = 100
	}
	return page, pageSize
}

func toTagVOs(tags []model.Tag) []dto.TagVO {
	vos := make([]dto.TagVO, 0, len(tags))
	for _, t := range tags {
		vos = append(vos, dto.TagVO{ID: t.ID, Name: t.Name, ArticleCount: t.ArticleCount})
	}
	return vos
}

func aiStatus(summary string) int {
	if strings.TrimSpace(summary) == "" {
		return 0
	}
	return 1
}

func parseIDs(idsStr string) ([]int64, error) {
	parts := strings.Split(idsStr, ",")
	ids := make([]int64, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p == "" {
			continue
		}
		id, err := strconv.ParseInt(p, 10, 64)
		if err != nil {
			return nil, err
		}
		ids = append(ids, id)
	}
	return ids, nil
}

func extractPlainText(markdown string) string {
	if markdown == "" {
		return ""
	}
	replacements := []struct {
		re   *regexp.Regexp
		sub  string
	}{
		{regexp.MustCompile(`#{1,6}\s*`), ""},
		{regexp.MustCompile(`\*\*(.+?)\*\*`), "$1"},
		{regexp.MustCompile(`\*(.+?)\*`), "$1"},
		{regexp.MustCompile("`{1,3}[^`]*`{1,3}"), ""},
		{regexp.MustCompile(`!\[.*?\]\(.*?\)`), ""},
		{regexp.MustCompile(`\[([^\]]*)\]\(.*?\)`), "$1"},
		{regexp.MustCompile(`>\s*`), ""},
		{regexp.MustCompile(`[-*+]\s+`), ""},
		{regexp.MustCompile(`\d+\.\s+`), ""},
		{regexp.MustCompile(`\|.*?\|`), ""},
		{regexp.MustCompile(`---+`), ""},
		{regexp.MustCompile(`\n+`), " "},
		{regexp.MustCompile(`\s+`), " "},
	}
	text := markdown
	for _, r := range replacements {
		text = r.re.ReplaceAllString(text, r.sub)
	}
	text = strings.TrimSpace(text)
	if len(text) > 200 {
		return text[:200]
	}
	return text
}
