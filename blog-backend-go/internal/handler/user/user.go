package user

import (
	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/handler"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

// UserHandler handles user profile APIs.
type UserHandler struct {
	userSvc *service.UserService
}

// NewUserHandler creates a UserHandler.
func NewUserHandler(userSvc *service.UserService) *UserHandler {
	return &UserHandler{userSvc: userSvc}
}

// Register registers user routes.
func (h *UserHandler) Register(r *gin.RouterGroup) {
	g := r.Group("/user")
	g.Use(middleware.RequireAuth())
	g.GET("/:userId", h.GetByID)
	g.GET("/info", h.GetInfo)
	g.PUT("/me", h.UpdateProfile)
	g.PUT("/me/password", h.ChangePassword)
}

// GetByID GET /user/:userId
func (h *UserHandler) GetByID(c *gin.Context) {
	userID, err := handler.ParseID(c.Param("userId"))
	if err != nil {
		resp.BadRequest(c, "invalid user id")
		return
	}
	user, err := h.userSvc.GetByID(c.Request.Context(), userID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, user)
}

// GetInfo GET /user/info
func (h *UserHandler) GetInfo(c *gin.Context) {
	userID, _ := middleware.GetUserID(c)
	user, err := h.userSvc.GetByID(c.Request.Context(), userID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, user)
}

// UpdateProfile PUT /user/me
func (h *UserHandler) UpdateProfile(c *gin.Context) {
	var req dto.UpdateProfileRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	userID, _ := middleware.GetUserID(c)
	user, err := h.userSvc.UpdateProfile(c.Request.Context(), userID, &req)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "更新成功", user)
}

// ChangePassword PUT /user/me/password
func (h *UserHandler) ChangePassword(c *gin.Context) {
	var req dto.ChangePasswordRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	userID, _ := middleware.GetUserID(c)
	if err := h.userSvc.ChangePassword(c.Request.Context(), userID, &req); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "密码修改成功", nil)
}
