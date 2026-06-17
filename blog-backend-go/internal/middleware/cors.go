package middleware

import (
	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/gin-gonic/gin"
)

// CORS configures cross-origin requests to match the original Spring Security setup.
func CORS(cfg *config.AppEnvConfig) gin.HandlerFunc {
	return func(c *gin.Context) {
		origin := c.Request.Header.Get("Origin")
		if isAllowedOrigin(origin, cfg.Domain) {
			c.Writer.Header().Set("Access-Control-Allow-Origin", origin)
		} else {
			c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		}
		c.Writer.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "*")
		c.Writer.Header().Set("Access-Control-Allow-Credentials", "true")
		c.Writer.Header().Set("Access-Control-Max-Age", "3600")

		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	}
}

func isAllowedOrigin(origin, appDomain string) bool {
	if origin == "" {
		return false
	}
	allowed := []string{
		"http://localhost",
		"http://127.0.0.1",
	}
	if appDomain != "" {
		allowed = append(allowed,
			"http://"+appDomain,
			"https://"+appDomain,
		)
	}
	for _, prefix := range allowed {
		if origin == prefix || (len(origin) > len(prefix) && origin[:len(prefix)] == prefix && origin[len(prefix)] == ':') {
			return true
		}
	}
	return false
}
