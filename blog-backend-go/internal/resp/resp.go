package resp

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
)

// Result is the unified API response envelope.
type Result[T any] struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
	Data    T      `json:"data"`
}

// PageResult is the unified paginated response.
type PageResult[T any] struct {
	Records  []T   `json:"records"`
	Total    int64 `json:"total"`
	Page     int   `json:"page"`
	PageSize int   `json:"pageSize"`
}

// Success returns a 200 success response.
func Success[T any](c *gin.Context, data T) {
	c.JSON(http.StatusOK, Result[T]{
		Code:    constant.CodeSuccess,
		Message: constant.MsgOK,
		Data:    data,
	})
}

// SuccessMsg returns a 200 response with custom message.
func SuccessMsg(c *gin.Context, message string, data any) {
	c.JSON(http.StatusOK, Result[any]{
		Code:    constant.CodeSuccess,
		Message: message,
		Data:    data,
	})
}

// Error returns a response with the given HTTP status and message.
func Error(c *gin.Context, httpStatus int, code int, message string) {
	c.JSON(httpStatus, Result[any]{
		Code:    code,
		Message: message,
		Data:    nil,
	})
}

// BadRequest shortcut.
func BadRequest(c *gin.Context, message string) {
	Error(c, http.StatusBadRequest, constant.CodeBadRequest, message)
}

// Unauthorized shortcut.
func Unauthorized(c *gin.Context, message string) {
	Error(c, http.StatusUnauthorized, constant.CodeUnauthorized, message)
}

// Forbidden shortcut.
func Forbidden(c *gin.Context, message string) {
	Error(c, http.StatusForbidden, constant.CodeForbidden, message)
}

// NotFound shortcut.
func NotFound(c *gin.Context, message string) {
	Error(c, http.StatusNotFound, constant.CodeNotFound, message)
}

// Conflict shortcut.
func Conflict(c *gin.Context, message string) {
	Error(c, http.StatusConflict, constant.CodeConflict, message)
}

// InternalError shortcut.
func InternalError(c *gin.Context) {
	Error(c, http.StatusInternalServerError, constant.CodeInternalError, constant.MsgInternalError)
}
