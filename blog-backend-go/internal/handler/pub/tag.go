package pub

import (
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

// TagHandler handles public tag APIs.
type TagHandler struct {
	tagSvc *service.TagService
}

// NewTagHandler creates a TagHandler.
func NewTagHandler(tagSvc *service.TagService) *TagHandler {
	return &TagHandler{tagSvc: tagSvc}
}

// Register registers public tag routes.
func (h *TagHandler) Register(r *gin.RouterGroup) {
	r.GET("/tags", h.GetList)
}

// GetList GET /tags
func (h *TagHandler) GetList(c *gin.Context) {
	tags, err := h.tagSvc.GetAllTags(c.Request.Context())
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, tags)
}
