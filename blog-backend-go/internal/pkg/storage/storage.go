package storage

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/qiniu/go-sdk/v7/auth/qbox"
	"github.com/qiniu/go-sdk/v7/storage"
)

// Uploader abstracts file upload to local disk or Qiniu OSS.
type Uploader struct {
	cfg *config.StorageConfig
}

// NewUploader creates a new uploader.
func NewUploader(cfg *config.StorageConfig) *Uploader {
	return &Uploader{cfg: cfg}
}

// Upload stores the file and returns the public URL.
func (u *Uploader) Upload(ctx context.Context, objectName string, data []byte) (string, error) {
	if strings.EqualFold(u.cfg.Type, "oss") {
		return u.uploadOSS(ctx, objectName, data)
	}
	return u.uploadLocal(objectName, data)
}

func (u *Uploader) uploadLocal(objectName string, data []byte) (string, error) {
	basePath := filepath.Clean(u.cfg.Local.Path)
	fullPath := filepath.Join(basePath, filepath.FromSlash(objectName))
	dir := filepath.Dir(fullPath)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return "", fmt.Errorf("create upload dir failed: %w", err)
	}
	if err := os.WriteFile(fullPath, data, 0644); err != nil {
		return "", fmt.Errorf("write upload file failed: %w", err)
	}
	return "/uploads/" + objectName, nil
}

func (u *Uploader) uploadOSS(ctx context.Context, objectName string, data []byte) (string, error) {
	prefix := strings.TrimRight(u.cfg.OSS.PathPrefix, "/")
	key := prefix + "/" + objectName

	putPolicy := storage.PutPolicy{
		Scope: fmt.Sprintf("%s:%s", u.cfg.OSS.BucketName, key),
	}
	mac := qbox.NewMac(u.cfg.OSS.AccessKey, u.cfg.OSS.SecretKey)
	upToken := putPolicy.UploadToken(mac)

	cfg := storage.Config{Zone: &storage.ZoneHuanan}
	formUploader := storage.NewFormUploader(&cfg)
	ret := storage.PutRet{}
	err := formUploader.Put(ctx, &ret, upToken, key, bytes.NewReader(data), int64(len(data)), nil)
	if err != nil {
		return "", fmt.Errorf("upload to oss failed: %w", err)
	}

	url := strings.TrimRight(u.cfg.OSS.Endpoint, "/") + "/" + key
	slog.Info("uploaded to oss", slog.String("url", url))
	return url, nil
}

// ServeLocalFile serves a local uploaded file over HTTP.
func (u *Uploader) ServeLocalFile(objectName string, w http.ResponseWriter, r *http.Request) {
	basePath := filepath.Clean(u.cfg.Local.Path)
	fullPath := filepath.Join(basePath, filepath.FromSlash(objectName))
	fullPath = filepath.Clean(fullPath)
	if !strings.HasPrefix(fullPath, basePath) {
		http.NotFound(w, r)
		return
	}
	http.ServeFile(w, r, fullPath)
}

// IsOSS returns true if OSS storage is configured.
func (u *Uploader) IsOSS() bool {
	return strings.EqualFold(u.cfg.Type, "oss")
}

// ReadAll reads all bytes from an io.Reader with a size limit.
func ReadAll(r io.Reader, limit int64) ([]byte, error) {
	return io.ReadAll(io.LimitReader(r, limit))
}

// GenerateObjectName creates a unique object name based on type, date and original extension.
func GenerateObjectName(fileType, ext string, now time.Time) string {
	dateStr := now.Format("2006/01/02")
	filename := fmt.Sprintf("%d%s", now.UnixNano(), ext)
	return fmt.Sprintf("%s/%s/%s", fileType, dateStr, filename)
}
