package user

import (
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/handler"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

// CommentHandler handles user comment APIs.
type CommentHandler struct {
	commentSvc *service.CommentService
}

// NewCommentHandler creates a user CommentHandler.
func NewCommentHandler(commentSvc *service.CommentService) *CommentHandler {
	return &CommentHandler{commentSvc: commentSvc}
}

// Register registers user comment routes.
func (h *CommentHandler) Register(r *gin.RouterGroup) {
	g := r.Group("/comments")
	g.Use(middleware.RequireAuth())
	g.POST("", h.Create)
	g.DELETE("/:commentId", h.Delete)
	g.GET("/replies", h.GetReplies)
	g.GET("/mine", h.GetMyComments)
}

// Create POST /comments
func (h *CommentHandler) Create(c *gin.Context) {
	var req dto.CommentCreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	userID, _ := middleware.GetUserID(c)
	comment, err := h.commentSvc.CreateComment(c.Request.Context(), userID, &req)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "评论成功", comment)
}

// Delete DELETE /comments/:commentId
func (h *CommentHandler) Delete(c *gin.Context) {
	commentID, err := handler.ParseID(c.Param("commentId"))
	if err != nil {
		resp.BadRequest(c, "invalid comment id")
		return
	}
	userID, _ := middleware.GetUserID(c)
	isAdmin := handler.IsCurrentUserAdmin(c)
	if err := h.commentSvc.DeleteComment(c.Request.Context(), commentID, userID, isAdmin); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "删除成功", nil)
}

// GetReplies GET /comments/replies
func (h *CommentHandler) GetReplies(c *gin.Context) {
	var q dto.ReplyQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	userID, _ := middleware.GetUserID(c)
	result, err := h.commentSvc.GetReplies(c.Request.Context(), userID, q.Page, q.PageSize, q.UnreadOnly == 1)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// GetMyComments GET /comments/mine
func (h *CommentHandler) GetMyComments(c *gin.Context) {
	var q dto.MyCommentQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	userID, _ := middleware.GetUserID(c)
	result, err := h.commentSvc.GetMyComments(c.Request.Context(), userID, q.Page, q.PageSize)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}
