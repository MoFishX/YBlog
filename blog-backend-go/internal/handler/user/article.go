package user

import (
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/handler"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

// ArticleHandler handles user article APIs.
type ArticleHandler struct {
	articleSvc *service.ArticleService
}

// NewArticleHandler creates a user ArticleHandler.
func NewArticleHandler(articleSvc *service.ArticleService) *ArticleHandler {
	return &ArticleHandler{articleSvc: articleSvc}
}

// Register registers user article routes.
func (h *ArticleHandler) Register(r *gin.RouterGroup) {
	g := r.Group("/articles")
	g.Use(middleware.RequireAuth())
	g.GET("/mine", h.GetMine)
	g.POST("", middleware.RequireActive(), h.Create)
	g.PUT("/:articleId", middleware.RequireActive(), h.Update)
	g.DELETE("/:articleId", middleware.RequireActive(), h.Delete)
	g.POST("/:articleId/like", h.Like)
	g.POST("/genSummaryLong", h.GenSummaryLong)
}

// GetMine GET /articles/mine
func (h *ArticleHandler) GetMine(c *gin.Context) {
	var q dto.ArticleQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	userID, _ := middleware.GetUserID(c)
	q.AuthorID = userID
	result, err := h.articleSvc.GetArticleList(c.Request.Context(), &q)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// Create POST /articles
func (h *ArticleHandler) Create(c *gin.Context) {
	var req dto.ArticleCreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	userID, _ := middleware.GetUserID(c)
	article, err := h.articleSvc.CreateArticle(c.Request.Context(), userID, &req)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "发布成功", article)
}

// Update PUT /articles/:articleId
func (h *ArticleHandler) Update(c *gin.Context) {
	articleID, err := handler.ParseID(c.Param("articleId"))
	if err != nil {
		resp.BadRequest(c, "invalid article id")
		return
	}
	var req dto.ArticleUpdateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	userID, _ := middleware.GetUserID(c)
	isAdmin := handler.IsCurrentUserAdmin(c)
	article, err := h.articleSvc.UpdateArticle(c.Request.Context(), articleID, userID, isAdmin, &req)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "更新成功", article)
}

// Delete DELETE /articles/:articleId
func (h *ArticleHandler) Delete(c *gin.Context) {
	articleID, err := handler.ParseID(c.Param("articleId"))
	if err != nil {
		resp.BadRequest(c, "invalid article id")
		return
	}
	userID, _ := middleware.GetUserID(c)
	if err := h.articleSvc.DeleteArticleOne(c.Request.Context(), articleID, userID); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "删除成功", nil)
}

// Like POST /articles/:articleId/like
func (h *ArticleHandler) Like(c *gin.Context) {
	articleID, err := handler.ParseID(c.Param("articleId"))
	if err != nil {
		resp.BadRequest(c, "invalid article id")
		return
	}
	userID, _ := middleware.GetUserID(c)
	article, err := h.articleSvc.ToggleLike(c.Request.Context(), articleID, userID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, article)
}

// GenSummaryLong POST /articles/genSummaryLong
func (h *ArticleHandler) GenSummaryLong(c *gin.Context) {
	articleID, err := handler.ParseIntQuery(c, "articleId")
	if err != nil {
		resp.BadRequest(c, "invalid article id")
		return
	}
	userID, _ := middleware.GetUserID(c)
	isAdmin := handler.IsCurrentUserAdmin(c)
	if err := h.articleSvc.TriggerAiSummaryLong(c.Request.Context(), userID, articleID, isAdmin); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "已提交AI总结任务", nil)
}
