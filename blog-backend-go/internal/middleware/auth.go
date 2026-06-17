package middleware

import (
	"strings"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/errs"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/jwt"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/redis"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/repository"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/gin-gonic/gin"
)

// ContextKey is the key type for context values.
type ContextKey string

const (
	// CtxUserID holds the current user id in gin context.
	CtxUserID ContextKey = "userId"
	// CtxUsername holds the current username in gin context.
	CtxUsername ContextKey = "username"
	// CtxRole holds the current user role in gin context.
	CtxRole ContextKey = "role"
)

// Auth extracts and validates the JWT from the Authorization header and loads user status.
func Auth(jwtMgr *jwt.Manager, rdb *redis.Client, userRepo *repository.UserRepo) gin.HandlerFunc {
	return func(c *gin.Context) {
		header := c.GetHeader("Authorization")
		if header == "" || !strings.HasPrefix(header, "Bearer ") {
			c.Next()
			return
		}
		token := strings.TrimPrefix(header, "Bearer ")

		// Check blacklist.
		blacklisted, err := rdb.Exists(c.Request.Context(), "blacklist:token:"+token)
		if err == nil && blacklisted {
			c.Next()
			return
		}

		claims, err := jwtMgr.Validate(token)
		if err != nil {
			c.Next()
			return
		}

		c.Set(string(CtxUserID), claims.UserID)
		c.Set(string(CtxUsername), claims.Username)
		c.Set(string(CtxRole), claims.Role)

		// Load user status to support active-status checks.
		if userRepo != nil {
			user, err := userRepo.GetByID(c.Request.Context(), claims.UserID)
			if err == nil && user != nil {
				c.Set("status", user.Status)
			}
		}
		c.Next()
	}
}

// RequireAuth aborts with 401 if the user is not authenticated.
func RequireAuth() gin.HandlerFunc {
	return func(c *gin.Context) {
		if _, ok := GetUserID(c); !ok {
			resp.Unauthorized(c, constant.MsgUnauthorized)
			c.Abort()
			return
		}
		c.Next()
	}
}

// RequireActive aborts with 403 if the user status is not ACTIVE.
func RequireActive() gin.HandlerFunc {
	return func(c *gin.Context) {
		status, ok := c.Get("status")
		if ok && status == constant.UserStatusActive {
			c.Next()
			return
		}
		resp.Forbidden(c, constant.MsgUserNotActivated)
		c.Abort()
	}
}

// RequireAdmin aborts with 403 if the user is not an ADMIN.
func RequireAdmin() gin.HandlerFunc {
	return func(c *gin.Context) {
		role, ok := GetRole(c)
		if !ok || role != constant.RoleAdmin {
			resp.Forbidden(c, constant.MsgForbidden)
			c.Abort()
			return
		}
		c.Next()
	}
}

// GetUserID returns the current user id from context.
func GetUserID(c *gin.Context) (int64, bool) {
	v, ok := c.Get(string(CtxUserID))
	if !ok {
		return 0, false
	}
	id, ok := v.(int64)
	return id, ok
}

// GetRole returns the current user role from context.
func GetRole(c *gin.Context) (string, bool) {
	v, ok := c.Get(string(CtxRole))
	if !ok {
		return "", false
	}
	role, ok := v.(string)
	return role, ok
}

// GetUsername returns the current username from context.
func GetUsername(c *gin.Context) (string, bool) {
	v, ok := c.Get(string(CtxUsername))
	if !ok {
		return "", false
	}
	name, ok := v.(string)
	return name, ok
}

// HandleBusinessError converts errs.BusinessError to a JSON response.
func HandleBusinessError() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()
		if len(c.Errors) > 0 {
			for _, e := range c.Errors {
				if be, ok := e.Err.(*errs.BusinessError); ok {
					resp.Error(c, be.Code, be.Code, be.Message)
					return
				}
			}
		}
	}
}
