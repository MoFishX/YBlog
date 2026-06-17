package service

import (
	"context"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/errs"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/model"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
)

// CommentService handles comment business logic.
type CommentService struct {
	commentRepo *repository.CommentRepo
	articleRepo *repository.ArticleRepo
	userRepo    *repository.UserRepo
}

// NewCommentService creates a CommentService.
func NewCommentService(commentRepo *repository.CommentRepo, articleRepo *repository.ArticleRepo, userRepo *repository.UserRepo) *CommentService {
	return &CommentService{commentRepo: commentRepo, articleRepo: articleRepo, userRepo: userRepo}
}

// GetCommentsByArticle returns paginated comments for an article.
func (s *CommentService) GetCommentsByArticle(ctx context.Context, articleID, userID int64, page, pageSize int) (*resp.PageResult[dto.CommentVO], error) {
	if _, err := s.articleRepo.GetByID(ctx, articleID); err != nil {
		return nil, err
	}
	comments, total, err := s.commentRepo.ListByArticle(ctx, articleID, userID, page, pageSize)
	if err != nil {
		return nil, err
	}
	vos := make([]dto.CommentVO, 0, len(comments))
	for _, c := range comments {
		vos = append(vos, s.toVO(ctx, &c, userID, false))
	}
	return &resp.PageResult[dto.CommentVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

// CreateComment creates a new comment.
func (s *CommentService) CreateComment(ctx context.Context, userID int64, req *dto.CommentCreateRequest) (dto.CommentVO, error) {
	if _, err := s.articleRepo.GetByID(ctx, req.ArticleID); err != nil {
		return dto.CommentVO{}, err
	}
	if req.ParentID != nil && *req.ParentID > 0 {
		parent, err := s.commentRepo.GetByID(ctx, *req.ParentID)
		if err != nil {
			return dto.CommentVO{}, err
		}
		if parent == nil || parent.Status == constant.CommentStatusHidden {
			return dto.CommentVO{}, errs.New(constant.CodeNotFound, constant.MsgCommentNotFound)
		}
	}

	comment := &model.Comment{
		ArticleID: req.ArticleID,
		UserID:    userID,
		Content:   req.Content,
		ParentID:  req.ParentID,
		Status:    constant.CommentStatusActive,
		IsRead:    0,
		CreatedAt: time.Now(),
	}
	if err := s.commentRepo.Create(ctx, comment); err != nil {
		return dto.CommentVO{}, err
	}
	return s.toVO(ctx, comment, userID, false), nil
}

// DeleteComment deletes a comment if the user has permission.
func (s *CommentService) DeleteComment(ctx context.Context, commentID, userID int64, isAdmin bool) error {
	comment, err := s.commentRepo.GetByID(ctx, commentID)
	if err != nil {
		return err
	}
	if comment == nil {
		return errs.New(constant.CodeNotFound, constant.MsgCommentNotFound)
	}
	if !isAdmin && comment.UserID != userID {
		article, err := s.articleRepo.GetByID(ctx, comment.ArticleID)
		if err != nil {
			return err
		}
		if article == nil || article.AuthorID != userID {
			return errs.New(constant.CodeForbidden, constant.MsgForbidden)
		}
	}
	return s.commentRepo.DeleteByID(ctx, commentID)
}

// HideComment toggles the hidden status of a comment.
func (s *CommentService) HideComment(ctx context.Context, commentID int64) error {
	comment, err := s.commentRepo.GetByID(ctx, commentID)
	if err != nil {
		return err
	}
	if comment == nil {
		return errs.New(constant.CodeNotFound, constant.MsgCommentNotFound)
	}
	if comment.Status == constant.CommentStatusHidden {
		comment.Status = constant.CommentStatusActive
	} else {
		comment.Status = constant.CommentStatusHidden
	}
	return s.commentRepo.Update(ctx, comment)
}

// ForceDeleteComment force deletes a comment.
func (s *CommentService) ForceDeleteComment(ctx context.Context, commentID int64) error {
	comment, err := s.commentRepo.GetByID(ctx, commentID)
	if err != nil {
		return err
	}
	if comment == nil {
		return errs.New(constant.CodeNotFound, constant.MsgCommentNotFound)
	}
	return s.commentRepo.DeleteByID(ctx, commentID)
}

// GetReplies returns comments replying to the current user's comments.
func (s *CommentService) GetReplies(ctx context.Context, userID int64, page, pageSize int, unreadOnly bool) (*resp.PageResult[dto.CommentVO], error) {
	comments, total, err := s.commentRepo.ListReplies(ctx, userID, unreadOnly, page, pageSize)
	if err != nil {
		return nil, err
	}
	vos := make([]dto.CommentVO, 0, len(comments))
	for _, c := range comments {
		vos = append(vos, s.toVO(ctx, &c, userID, true))
	}
	return &resp.PageResult[dto.CommentVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

// GetMyComments returns the current user's comments.
func (s *CommentService) GetMyComments(ctx context.Context, userID int64, page, pageSize int) (*resp.PageResult[dto.CommentVO], error) {
	comments, total, err := s.commentRepo.ListByUser(ctx, userID, page, pageSize)
	if err != nil {
		return nil, err
	}
	vos := make([]dto.CommentVO, 0, len(comments))
	for _, c := range comments {
		vos = append(vos, s.toVO(ctx, &c, userID, false))
	}
	return &resp.PageResult[dto.CommentVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

// GetAllComments returns paginated comments for admin.
func (s *CommentService) GetAllComments(ctx context.Context, page, pageSize int, keyword string, articleID int64) (*resp.PageResult[dto.CommentVO], error) {
	comments, total, err := s.commentRepo.ListAll(ctx, keyword, articleID, page, pageSize)
	if err != nil {
		return nil, err
	}
	vos := make([]dto.CommentVO, 0, len(comments))
	for _, c := range comments {
		vos = append(vos, s.toAdminVO(ctx, &c))
	}
	return &resp.PageResult[dto.CommentVO]{Records: vos, Total: total, Page: page, PageSize: pageSize}, nil
}

func (s *CommentService) toVO(ctx context.Context, c *model.Comment, currentUserID int64, includeArticle bool) dto.CommentVO {
	vo := dto.CommentVO{
		ID:        c.ID,
		Content:   c.Content,
		User:      s.buildAuthorVO(ctx, c.UserID),
		ArticleID: c.ArticleID,
		ParentID:  c.ParentID,
		IsRead:    c.IsRead == 1,
		Status:    c.Status,
		CreatedAt: c.CreatedAt,
	}
	if c.ParentID != nil && *c.ParentID > 0 {
		vo.ReplyTo = s.buildReplyToVO(ctx, *c.ParentID, currentUserID)
	}
	if includeArticle {
		article, _ := s.articleRepo.GetByID(ctx, c.ArticleID)
		if article != nil {
			vo.ArticleTitle = article.Title
		}
	}
	return vo
}

func (s *CommentService) toAdminVO(ctx context.Context, c *model.Comment) dto.CommentVO {
	vo := dto.CommentVO{
		ID:        c.ID,
		Content:   c.Content,
		User:      s.buildAuthorVO(ctx, c.UserID),
		ArticleID: c.ArticleID,
		Status:    c.Status,
		CreatedAt: c.CreatedAt,
	}
	article, _ := s.articleRepo.GetByID(ctx, c.ArticleID)
	if article != nil {
		vo.ArticleTitle = article.Title
	}
	return vo
}

func (s *CommentService) buildAuthorVO(ctx context.Context, userID int64) *dto.AuthorVO {
	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil || user == nil {
		return nil
	}
	return &dto.AuthorVO{ID: user.ID, Username: user.Username, Avatar: user.Avatar, Email: user.Email}
}

func (s *CommentService) buildReplyToVO(ctx context.Context, parentID, currentUserID int64) *dto.AuthorVO {
	parent, err := s.commentRepo.GetByID(ctx, parentID)
	if err != nil || parent == nil {
		return nil
	}
	if parent.Status == constant.CommentStatusHidden {
		if currentUserID <= 0 || parent.UserID != currentUserID {
			return nil
		}
	}
	return s.buildAuthorVO(ctx, parent.UserID)
}
