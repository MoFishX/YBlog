package pub

import (
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

// CommentHandler handles public comment APIs.
type CommentHandler struct {
	commentSvc *service.CommentService
}

// NewCommentHandler creates a CommentHandler.
func NewCommentHandler(commentSvc *service.CommentService) *CommentHandler {
	return &CommentHandler{commentSvc: commentSvc}
}

// Register registers public comment routes.
func (h *CommentHandler) Register(r *gin.RouterGroup) {
	r.GET("/comments", h.GetList)
}

// GetList GET /comments
func (h *CommentHandler) GetList(c *gin.Context) {
	var q dto.CommentQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if q.PageSize > 100 {
		q.PageSize = 100
	}
	userID, _ := middleware.GetUserID(c)
	result, err := h.commentSvc.GetCommentsByArticle(c.Request.Context(), q.ArticleID, userID, q.Page, q.PageSize)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}
