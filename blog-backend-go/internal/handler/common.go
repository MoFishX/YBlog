package handler

import (
	"strconv"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/gin-gonic/gin"
)

// ParseID parses an int64 path parameter.
func ParseID(s string) (int64, error) {
	return strconv.ParseInt(s, 10, 64)
}

// ParseIntQuery parses an int64 query parameter.
func ParseIntQuery(c *gin.Context, key string) (int64, error) {
	return strconv.ParseInt(c.Query(key), 10, 64)
}

// IsCurrentUserAdmin reports whether the authenticated user is an admin.
func IsCurrentUserAdmin(c *gin.Context) bool {
	role, _ := middleware.GetRole(c)
	return role == "ADMIN"
}
