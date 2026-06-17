package middleware

import (
	"log/slog"
	"net/http"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/errs"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
)

// Recovery recovers from panics and returns 500.
func Recovery() gin.HandlerFunc {
	return gin.CustomRecovery(func(c *gin.Context, err any) {
		slog.Error("panic recovered", slog.Any("error", err))
		resp.InternalError(c)
		c.Abort()
	})
}

// ErrorHandler translates known errors to JSON responses.
func ErrorHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()
		if len(c.Errors) == 0 {
			return
		}

		lastErr := c.Errors.Last().Err
		if be, ok := lastErr.(*errs.BusinessError); ok {
			status := httpStatusFromCode(be.Code)
			resp.Error(c, status, be.Code, be.Message)
			c.Abort()
			return
		}

		if ve, ok := lastErr.(validator.ValidationErrors); ok {
			msg := "参数校验失败"
			if len(ve) > 0 {
				msg = ve[0].Field() + ": " + ve[0].Tag()
			}
			resp.BadRequest(c, msg)
			c.Abort()
			return
		}

		slog.Error("unhandled error", slog.String("error", lastErr.Error()))
		resp.InternalError(c)
		c.Abort()
	}
}

func httpStatusFromCode(code int) int {
	if code >= 400 && code < 600 {
		return code
	}
	return http.StatusInternalServerError
}
