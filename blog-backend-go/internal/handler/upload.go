package handler

import (
	"io"
	"path/filepath"
	"strings"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/constant"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/middleware"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/pkg/storage"
	"github.com/MoFishX/YBlog/blog-backend-go/internal/resp"
	"github.com/gin-gonic/gin"
)

var allowedExtensions = map[string]bool{
	"jpg": true, "jpeg": true, "png": true, "gif": true, "webp": true,
}

const maxFileSize = 10 * 1024 * 1024

// UploadHandler handles file uploads.
type UploadHandler struct {
	uploader *storage.Uploader
}

// NewUploadHandler creates an UploadHandler.
func NewUploadHandler(uploader *storage.Uploader) *UploadHandler {
	return &UploadHandler{uploader: uploader}
}

// Register registers upload routes.
func (h *UploadHandler) Register(r *gin.RouterGroup) {
	r.POST("/upload", middleware.RequireAuth(), middleware.RequireActive(), h.Upload)
}

// Upload POST /upload
func (h *UploadHandler) Upload(c *gin.Context) {
	fileType := c.PostForm("type")
	if fileType != "avatar" && fileType != "article" {
		resp.BadRequest(c, constant.MsgFileTypeInvalid)
		return
	}

	file, header, err := c.Request.FormFile("file")
	if err != nil {
		resp.BadRequest(c, "missing file")
		return
	}
	defer file.Close()

	if header.Size > maxFileSize {
		resp.BadRequest(c, constant.MsgFileTooLarge)
		return
	}

	ext := fileExt(header.Filename)
	if ext == "" || !allowedExtensions[ext] {
		resp.BadRequest(c, constant.MsgFileTypeInvalid)
		return
	}

	data, err := io.ReadAll(file)
	if err != nil {
		resp.InternalError(c)
		return
	}

	objectName := storage.GenerateObjectName(fileType, ext, time.Now())
	url, err := h.uploader.Upload(c.Request.Context(), objectName, data)
	if err != nil {
		_ = c.Error(err)
		return
	}

	resp.SuccessMsg(c, "上传成功", gin.H{
		"url":      url,
		"filename": filepath.Base(objectName),
		"size":     header.Size,
	})
}

func fileExt(filename string) string {
	if !strings.Contains(filename, ".") {
		return ""
	}
	ext := strings.ToLower(filepath.Ext(filename))
	return strings.TrimPrefix(ext, ".")
}

// StaticHandler serves uploaded files for local storage.
func StaticHandler(uploader *storage.Uploader) gin.HandlerFunc {
	return func(c *gin.Context) {
		objectName := strings.TrimPrefix(c.Param("filepath"), "/")
		uploader.ServeLocalFile(objectName, c.Writer, c.Request)
	}
}
