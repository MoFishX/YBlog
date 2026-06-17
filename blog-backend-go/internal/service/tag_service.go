package service

import (
	"context"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/errs"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
)

// TagService handles tag business logic.
type TagService struct {
	tagRepo       *repository.TagRepo
	articleTagRepo *repository.ArticleTagRepo
}

// NewTagService creates a TagService.
func NewTagService(tagRepo *repository.TagRepo, articleTagRepo *repository.ArticleTagRepo) *TagService {
	return &TagService{tagRepo: tagRepo, articleTagRepo: articleTagRepo}
}

// GetAllTags returns all tags with article counts.
func (s *TagService) GetAllTags(ctx context.Context) ([]dto.TagVO, error) {
	tags, err := s.tagRepo.ListAllWithArticleCount(ctx)
	if err != nil {
		return nil, err
	}
	vos := make([]dto.TagVO, 0, len(tags))
	for _, tag := range tags {
		vos = append(vos, dto.TagVO{
			ID:           tag.ID,
			Name:         tag.Name,
			ArticleCount: tag.ArticleCount,
		})
	}
	return vos, nil
}

// GetAllTagsPaged returns paginated tags, optionally including createdBy.
func (s *TagService) GetAllTagsPaged(ctx context.Context, page, pageSize int, includeCreatedBy bool) (*resp.PageResult[dto.TagVO], error) {
	tags, err := s.tagRepo.ListAllWithArticleCount(ctx)
	if err != nil {
		return nil, err
	}
	total := int64(len(tags))
	from := (page - 1) * pageSize
	var paged []model.Tag
	if from < len(tags) {
		to := from + pageSize
		if to > len(tags) {
			to = len(tags)
		}
		paged = tags[from:to]
	}

	vos := make([]dto.TagVO, 0, len(paged))
	for _, tag := range paged {
		vo := dto.TagVO{
			ID:           tag.ID,
			Name:         tag.Name,
			ArticleCount: tag.ArticleCount,
		}
		if includeCreatedBy {
			vo.CreatedBy = tag.CreatedBy
		}
		vos = append(vos, vo)
	}
	return &resp.PageResult[dto.TagVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

// CreateTag creates a new tag.
func (s *TagService) CreateTag(ctx context.Context, name string, createdBy int64) (*dto.TagVO, error) {
	name = constant.Trim(name)
	if name == "" {
		return nil, errs.New(constant.CodeBadRequest, constant.MsgBadRequest)
	}
	count, err := s.tagRepo.CountByName(ctx, name, 0)
	if err != nil {
		return nil, err
	}
	if count > 0 {
		return nil, errs.New(constant.CodeConflict, constant.MsgTagNameExists)
	}
	tag := &model.Tag{Name: name, CreatedBy: createdBy}
	if err := s.tagRepo.Create(ctx, tag); err != nil {
		return nil, err
	}
	return &dto.TagVO{ID: tag.ID, Name: tag.Name, ArticleCount: 0, CreatedBy: tag.CreatedBy}, nil
}

// UpdateTag updates a tag name.
func (s *TagService) UpdateTag(ctx context.Context, tagID int64, name string) (*dto.TagVO, error) {
	name = constant.Trim(name)
	if name == "" {
		return nil, errs.New(constant.CodeBadRequest, constant.MsgBadRequest)
	}
	tag, err := s.tagRepo.GetByID(ctx, tagID)
	if err != nil {
		return nil, err
	}
	if tag == nil {
		return nil, errs.New(constant.CodeNotFound, constant.MsgNotFound)
	}
	count, err := s.tagRepo.CountByName(ctx, name, tagID)
	if err != nil {
		return nil, err
	}
	if count > 0 {
		return nil, errs.New(constant.CodeConflict, constant.MsgTagNameExists)
	}
	tag.Name = name
	if err := s.tagRepo.Update(ctx, tag); err != nil {
		return nil, err
	}
	countArticles, _ := s.tagRepo.TagArticleCount(ctx, tag.ID)
	return &dto.TagVO{ID: tag.ID, Name: tag.Name, ArticleCount: int(countArticles), CreatedBy: tag.CreatedBy}, nil
}

// DeleteTag deletes a tag and its associations.
func (s *TagService) DeleteTag(ctx context.Context, tagID int64) error {
	if err := s.articleTagRepo.DeleteByTagID(ctx, tagID); err != nil {
		return err
	}
	return s.tagRepo.DeleteByID(ctx, tagID)
}

// ResolveTagIDs resolves tag names to ids, creating new tags when needed.
func (s *TagService) ResolveTagIDs(ctx context.Context, tagNames []string, userID int64) ([]int64, error) {
	if len(tagNames) == 0 {
		return []int64{}, nil
	}
	ids := make([]int64, 0, len(tagNames))
	for _, name := range tagNames {
		name = constant.Trim(name)
		if name == "" {
			continue
		}
		existing, err := s.tagRepo.GetByName(ctx, name)
		if err != nil {
			return nil, err
		}
		if existing != nil {
			ids = append(ids, existing.ID)
			continue
		}
		newTag := &model.Tag{Name: name, CreatedBy: userID}
		if err := s.tagRepo.Create(ctx, newTag); err != nil {
			return nil, err
		}
		ids = append(ids, newTag.ID)
	}
	return ids, nil
}
