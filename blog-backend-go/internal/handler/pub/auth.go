package pub

import (
	"net/http"
	"strconv"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/dto"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

const refreshTokenCookie = "refreshToken"

// AuthHandler handles public authentication APIs.
type AuthHandler struct {
	authSvc *service.AuthService
}

// NewAuthHandler creates an AuthHandler.
func NewAuthHandler(authSvc *service.AuthService) *AuthHandler {
	return &AuthHandler{authSvc: authSvc}
}

// RegisterRoutes registers public auth routes.
func (h *AuthHandler) RegisterRoutes(r *gin.RouterGroup) {
	g := r.Group("/auth")
	g.POST("/login", h.Login)
	g.POST("/register", h.Register)
	g.POST("/refresh", h.Refresh)
	g.POST("/logout", h.Logout)
	g.GET("/verify-email", h.VerifyEmail)
	g.POST("/resend-activation", h.ResendActivation)
}

// Login POST /auth/login
func (h *AuthHandler) Login(c *gin.Context) {
	var req dto.LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	result, err := h.authSvc.Login(c.Request.Context(), &req)
	if err != nil {
		_ = c.Error(err)
		return
	}
	setRefreshTokenCookie(c, result.RefreshToken)
	resp.Success(c, result)
}

// Register POST /auth/register
func (h *AuthHandler) Register(c *gin.Context) {
	var req dto.RegisterRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if err := h.authSvc.Register(c.Request.Context(), &req); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "注册成功", nil)
}

// Refresh POST /auth/refresh
func (h *AuthHandler) Refresh(c *gin.Context) {
	refreshToken, err := c.Cookie(refreshTokenCookie)
	if err != nil || refreshToken == "" {
		resp.Unauthorized(c, "缺少刷新令牌")
		return
	}
	result, err := h.authSvc.RefreshToken(c.Request.Context(), refreshToken)
	if err != nil {
		_ = c.Error(err)
		return
	}
	resp.Success(c, result)
}

// Logout POST /auth/logout
func (h *AuthHandler) Logout(c *gin.Context) {
	authHeader := c.GetHeader("Authorization")
	if len(authHeader) > 7 && authHeader[:7] == "Bearer " {
		_ = h.authSvc.Logout(c.Request.Context(), authHeader[7:])
	}
	clearRefreshTokenCookie(c)
	resp.SuccessMsg(c, "已登出", nil)
}

// VerifyEmail GET /auth/verify-email
func (h *AuthHandler) VerifyEmail(c *gin.Context) {
	var q dto.VerifyEmailQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if err := h.authSvc.VerifyEmail(c.Request.Context(), q.Token); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "邮箱激活成功", nil)
}

// ResendActivation POST /auth/resend-activation
func (h *AuthHandler) ResendActivation(c *gin.Context) {
	var q dto.ResendActivationQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		resp.BadRequest(c, err.Error())
		return
	}
	if err := h.authSvc.ResendActivation(c.Request.Context(), q.Email); err != nil {
		_ = c.Error(err)
		return
	}
	resp.SuccessMsg(c, "激活邮件已重新发送", nil)
}

func setRefreshTokenCookie(c *gin.Context, token string) {
	maxAge, _ := strconv.Atoi(c.Request.Header.Get("X-Refresh-Max-Age"))
	if maxAge <= 0 {
		maxAge = 7 * 24 * 3600
	}
	c.SetSameSite(http.SameSiteLaxMode)
	c.SetCookie(refreshTokenCookie, token, maxAge, "/", "", false, true)
}

func clearRefreshTokenCookie(c *gin.Context) {
	c.SetSameSite(http.SameSiteLaxMode)
	c.SetCookie(refreshTokenCookie, "", -1, "/", "", false, true)
}
