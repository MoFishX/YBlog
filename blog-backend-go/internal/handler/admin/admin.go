package admin

import (
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/handler"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

// Handler aggregates admin handlers.
type Handler struct {
	adminSvc   *service.AdminService
	articleSvc *service.ArticleService
	commentSvc *service.CommentService
	tagSvc     *service.TagService
}

// NewHandler creates an admin Handler.
func NewHandler(adminSvc *service.AdminService, articleSvc *service.ArticleService, commentSvc *service.CommentService, tagSvc *service.TagService) *Handler {
	return &Handler{adminSvc: adminSvc, articleSvc: articleSvc, commentSvc: commentSvc, tagSvc: tagSvc}
}

// Register registers admin routes.
func (h *Handler) Register(r *gin.RouterGroup) {
	g := r.Group("/admin")
	g.Use(middleware.RequireAuth(), middleware.RequireAdmin())

	// users
	g.GET("/users", h.GetUserList)
	g.PUT("/users/:userId/status", h.UpdateUserStatus)
	g.PUT("/users/:userId/role", h.UpdateUserRole)

	// tags
	g.GET("/tags", h.GetTagList)
	g.POST("/tags", h.CreateTag)
	g.PUT("/tags/:tagId", h.UpdateTag)
	g.DELETE("/tags/:tagId", h.DeleteTag)

	// stats
	g.GET("/stats", h.GetStats)
	g.GET("/stats/weekly-trend", h.GetWeeklyTrend)

	// comments
	g.GET("/comments", h.GetCommentList)
	g.PUT("/comments/:commentId/hide", h.HideComment)
	g.DELETE("/comments/:commentId", h.ForceDeleteComment)

	// articles
	g.GET("/articles", h.GetArticleList)
	g.DELETE("/articles", h.ForceDeleteArticles)
}

// GetUserList GET /admin/users
func (h *Handler) GetUserList(c *gin.Context) {
	var q dto.AdminUserQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	result, err := h.adminSvc.GetUserList(c.Request.Context(), q.Page, q.PageSize, q.Keyword)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// UpdateUserStatus PUT /admin/users/:userId/status
func (h *Handler) UpdateUserStatus(c *gin.Context) {
	userID, err := handler.ParseID(c.Param("userId"))
	if err != nil {
		resp.BadRequest(c, "invalid user id")
		return
	}
	var req dto.UpdateUserStatusRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if err := h.adminSvc.UpdateUserStatus(c.Request.Context(), userID, req.Status); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "操作成功", gin.H{"id": userID, "status": req.Status})
}

// UpdateUserRole PUT /admin/users/:userId/role
func (h *Handler) UpdateUserRole(c *gin.Context) {
	userID, err := handler.ParseID(c.Param("userId"))
	if err != nil {
		resp.BadRequest(c, "invalid user id")
		return
	}
	var req dto.UpdateUserRoleRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if err := h.adminSvc.UpdateUserRole(c.Request.Context(), userID, req.Role); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "修改成功", gin.H{"id": userID, "role": req.Role})
}

// GetTagList GET /admin/tags
func (h *Handler) GetTagList(c *gin.Context) {
	page := c.DefaultQuery("page", "1")
	pageSize := c.DefaultQuery("pageSize", "20")
	p, _ := handler.ParseID(page)
	ps, _ := handler.ParseID(pageSize)
	result, err := h.tagSvc.GetAllTagsPaged(c.Request.Context(), int(p), int(ps), true)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// CreateTag POST /admin/tags
func (h *Handler) CreateTag(c *gin.Context) {
	var req dto.TagRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	tag, err := h.tagSvc.CreateTag(c.Request.Context(), req.Name, -1)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "创建成功", tag)
}

// UpdateTag PUT /admin/tags/:tagId
func (h *Handler) UpdateTag(c *gin.Context) {
	tagID, err := handler.ParseID(c.Param("tagId"))
	if err != nil {
		resp.BadRequest(c, "invalid tag id")
		return
	}
	var req dto.TagRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	tag, err := h.tagSvc.UpdateTag(c.Request.Context(), tagID, req.Name)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "更新成功", tag)
}

// DeleteTag DELETE /admin/tags/:tagId
func (h *Handler) DeleteTag(c *gin.Context) {
	tagID, err := handler.ParseID(c.Param("tagId"))
	if err != nil {
		resp.BadRequest(c, "invalid tag id")
		return
	}
	if err := h.tagSvc.DeleteTag(c.Request.Context(), tagID); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "删除成功", nil)
}

// GetStats GET /admin/stats
func (h *Handler) GetStats(c *gin.Context) {
	stats, err := h.adminSvc.GetStats(c.Request.Context())
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, stats)
}

// GetWeeklyTrend GET /admin/stats/weekly-trend
func (h *Handler) GetWeeklyTrend(c *gin.Context) {
	trends, err := h.adminSvc.GetWeeklyTrend(c.Request.Context())
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, trends)
}

// GetCommentList GET /admin/comments
func (h *Handler) GetCommentList(c *gin.Context) {
	var q dto.AdminCommentQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	result, err := h.commentSvc.GetAllComments(c.Request.Context(), q.Page, q.PageSize, q.Keyword, q.ArticleID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// HideComment PUT /admin/comments/:commentId/hide
func (h *Handler) HideComment(c *gin.Context) {
	commentID, err := handler.ParseID(c.Param("commentId"))
	if err != nil {
		resp.BadRequest(c, "invalid comment id")
		return
	}
	if err := h.commentSvc.HideComment(c.Request.Context(), commentID); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "操作成功", nil)
}

// ForceDeleteComment DELETE /admin/comments/:commentId
func (h *Handler) ForceDeleteComment(c *gin.Context) {
	commentID, err := handler.ParseID(c.Param("commentId"))
	if err != nil {
		resp.BadRequest(c, "invalid comment id")
		return
	}
	if err := h.commentSvc.ForceDeleteComment(c.Request.Context(), commentID); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "删除成功", nil)
}

// GetArticleList GET /admin/articles
func (h *Handler) GetArticleList(c *gin.Context) {
	var q dto.ArticleQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	result, err := h.articleSvc.GetAllArticles(c.Request.Context(), q.Page, q.PageSize, q.Status, q.Keyword)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// ForceDeleteArticles DELETE /admin/articles
func (h *Handler) ForceDeleteArticles(c *gin.Context) {
	ids := c.Query("ids")
	if ids == "" {
		resp.BadRequest(c, "ids required")
		return
	}
	if err := h.articleSvc.DeleteArticle(c.Request.Context(), ids, 0, true); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "删除成功", nil)
}
