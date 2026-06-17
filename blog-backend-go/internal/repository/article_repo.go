package repository

import (
	"context"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"gorm.io/gorm"
)

// ArticleRepo handles article data access.
type ArticleRepo struct {
	db *gorm.DB
}

// NewArticleRepo creates an ArticleRepo.
func NewArticleRepo(db *gorm.DB) *ArticleRepo {
	return &ArticleRepo{db: db}
}

// Create inserts a new article.
func (r *ArticleRepo) Create(ctx context.Context, article *model.Article) error {
	return r.db.WithContext(ctx).Create(article).Error
}

// GetByID retrieves an article by id.
func (r *ArticleRepo) GetByID(ctx context.Context, id int64) (*model.Article, error) {
	var article model.Article
	err := r.db.WithContext(ctx).First(&article, id).Error
	if err == gorm.ErrRecordNotFound {
		return nil, nil
	}
	return &article, err
}

// Update updates an article.
func (r *ArticleRepo) Update(ctx context.Context, article *model.Article) error {
	return r.db.WithContext(ctx).Save(article).Error
}

// DeleteByIDs deletes articles by ids within a transaction.
func (r *ArticleRepo) DeleteByIDs(ctx context.Context, ids []int64) error {
	return r.db.WithContext(ctx).Where("id IN ?", ids).Delete(&model.Article{}).Error
}

// IncrementViewCount increments the view count by delta.
func (r *ArticleRepo) IncrementViewCount(ctx context.Context, id, delta int64) error {
	return r.db.WithContext(ctx).Model(&model.Article{}).Where("id = ?", id).
		UpdateColumn("view_count", gorm.Expr("view_count + ?", delta)).Error
}

// IncrementLikeCount increments the like count by delta.
func (r *ArticleRepo) IncrementLikeCount(ctx context.Context, id, delta int64) error {
	return r.db.WithContext(ctx).Model(&model.Article{}).Where("id = ?", id).
		UpdateColumn("like_count", gorm.Expr("like_count + ?", delta)).Error
}

// CountAll returns total article count.
func (r *ArticleRepo) CountAll(ctx context.Context) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.Article{}).Count(&count).Error
	return count, err
}

// SumViewCount returns the sum of view counts.
func (r *ArticleRepo) SumViewCount(ctx context.Context) (int64, error) {
	var total int64
	err := r.db.WithContext(ctx).Model(&model.Article{}).Select("COALESCE(SUM(view_count), 0)").Scan(&total).Error
	return total, err
}

// SumLikeCount returns the sum of like counts.
func (r *ArticleRepo) SumLikeCount(ctx context.Context) (int64, error) {
	var total int64
	err := r.db.WithContext(ctx).Model(&model.Article{}).Select("COALESCE(SUM(like_count), 0)").Scan(&total).Error
	return total, err
}

// CountToday returns the number of articles created today.
func (r *ArticleRepo) CountToday(ctx context.Context) (int64, error) {
	var count int64
	err := r.db.WithContext(ctx).Model(&model.Article{}).
		Where("DATE(created_at) = CURDATE()").Count(&count).Error
	return count, err
}

// List lists articles with optional filters.
func (r *ArticleRepo) List(ctx context.Context, status string, authorID int64, orderBy string, page, pageSize int) ([]model.Article, int64, error) {
	var articles []model.Article
	var total int64
	db := r.db.WithContext(ctx).Model(&model.Article{})
	if status != "" {
		db = db.Where("status = ?", status)
	}
	if authorID > 0 {
		db = db.Where("author_id = ?", authorID)
	}
	switch orderBy {
	case "hot":
		db = db.Order("view_count DESC")
	default:
		db = db.Order("created_at DESC")
	}
	err := db.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	err = db.Offset((page - 1) * pageSize).Limit(pageSize).Find(&articles).Error
	return articles, total, err
}

// ListByTagName lists articles filtered by tag name.
func (r *ArticleRepo) ListByTagName(ctx context.Context, tagName, status string, orderBy string, page, pageSize int) ([]model.Article, int64, error) {
	var articles []model.Article
	var total int64

	subQuery := r.db.Table("article_tag at").
		Select("at.article_id").
		Joins("JOIN tag t ON at.tag_id = t.id").
		Where("t.name = ?", tagName)

	db := r.db.WithContext(ctx).Model(&model.Article{}).
		Where("id IN (?)", subQuery)
	if status != "" {
		db = db.Where("status = ?", status)
	}
	switch orderBy {
	case "hot":
		db = db.Order("view_count DESC")
	default:
		db = db.Order("created_at DESC")
	}
	err := db.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	err = db.Offset((page - 1) * pageSize).Limit(pageSize).Find(&articles).Error
	return articles, total, err
}

// ListAll lists all articles for admin with optional status and keyword.
func (r *ArticleRepo) ListAll(ctx context.Context, status, keyword string, page, pageSize int) ([]model.Article, int64, error) {
	var articles []model.Article
	var total int64
	db := r.db.WithContext(ctx).Model(&model.Article{}).Order("created_at DESC")
	if status != "" {
		db = db.Where("status = ?", status)
	}
	if keyword != "" {
		db = db.Where("title LIKE ?", "%"+keyword+"%")
	}
	err := db.Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	err = db.Offset((page - 1) * pageSize).Limit(pageSize).Find(&articles).Error
	return articles, total, err
}

// Search searches published articles by keyword using MySQL FULLTEXT.
func (r *ArticleRepo) Search(ctx context.Context, keyword string, offset, pageSize int) ([]model.Article, int64, error) {
	var articles []model.Article
	var total int64

	countSQL := `SELECT COUNT(*) FROM article a LEFT JOIN article_content ac ON a.id = ac.article_id
		WHERE a.status = 'PUBLISHED'
		AND (MATCH(a.title, a.summary) AGAINST(? IN NATURAL LANGUAGE MODE)
		OR MATCH(ac.content) AGAINST(? IN NATURAL LANGUAGE MODE))`
	err := r.db.WithContext(ctx).Raw(countSQL, keyword, keyword).Scan(&total).Error
	if err != nil {
		return nil, 0, err
	}

	searchSQL := `SELECT a.*, MATCH(a.title, a.summary) AGAINST(? IN NATURAL LANGUAGE MODE) AS relevance
		FROM article a LEFT JOIN article_content ac ON a.id = ac.article_id
		WHERE a.status = 'PUBLISHED'
		AND (MATCH(a.title, a.summary) AGAINST(? IN NATURAL LANGUAGE MODE)
		OR MATCH(ac.content) AGAINST(? IN NATURAL LANGUAGE MODE))
		ORDER BY relevance DESC LIMIT ? OFFSET ?`
	err = r.db.WithContext(ctx).Raw(searchSQL, keyword, keyword, keyword, pageSize, offset).Scan(&articles).Error
	return articles, total, err
}
