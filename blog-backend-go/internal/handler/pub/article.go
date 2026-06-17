package pub

import (
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/handler"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

// ArticleHandler handles public article APIs.
type ArticleHandler struct {
	articleSvc *service.ArticleService
}

// NewArticleHandler creates an ArticleHandler.
func NewArticleHandler(articleSvc *service.ArticleService) *ArticleHandler {
	return &ArticleHandler{articleSvc: articleSvc}
}

// Register registers public article routes.
func (h *ArticleHandler) Register(r *gin.RouterGroup) {
	g := r.Group("/articles")
	g.GET("", h.GetList)
	g.GET("/hot", h.GetHot)
	g.GET("/search", h.Search)
	g.GET("/:articleId", h.GetDetail)
	g.GET("/getAiSummaryLong", h.GetAiSummaryLong)
}

// GetList GET /articles
func (h *ArticleHandler) GetList(c *gin.Context) {
	var q dto.ArticleQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if q.PageSize > 100 {
		q.PageSize = 100
	}
	result, err := h.articleSvc.GetArticleList(c.Request.Context(), &q)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// GetHot GET /articles/hot
func (h *ArticleHandler) GetHot(c *gin.Context) {
	var q dto.HotArticleQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if q.Limit > 50 {
		q.Limit = 50
	}
	articles, err := h.articleSvc.GetHotArticles(c.Request.Context(), q.Limit)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, articles)
}

// Search GET /articles/search
func (h *ArticleHandler) Search(c *gin.Context) {
	var q dto.SearchArticleQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if q.PageSize > 50 {
		q.PageSize = 50
	}
	result, err := h.articleSvc.Search(c.Request.Context(), q.Keyword, q.Page, q.PageSize)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// GetDetail GET /articles/:articleId
func (h *ArticleHandler) GetDetail(c *gin.Context) {
	articleID, err := handler.ParseID(c.Param("articleId"))
	if err != nil {
		resp.BadRequest(c, "invalid article id")
		return
	}
	userID, _ := middleware.GetUserID(c)
	isAdmin := handler.IsCurrentUserAdmin(c)
	article, err := h.articleSvc.GetArticleDetail(c.Request.Context(), articleID, userID, isAdmin)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, article)
}

// GetAiSummaryLong GET /articles/getAiSummaryLong
func (h *ArticleHandler) GetAiSummaryLong(c *gin.Context) {
	articleID, err := handler.ParseIntQuery(c, "articleId")
	if err != nil {
		resp.BadRequest(c, "invalid article id")
		return
	}
	summary, err := h.articleSvc.GetAiSummaryLong(c.Request.Context(), articleID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, summary)
}
