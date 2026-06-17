package constant

import "strings"

// Trim is a helper to trim whitespace from strings.
func Trim(s string) string {
	return strings.TrimSpace(s)
}

// Roles
const (
	RoleUser  = "USER"
	RoleAdmin = "ADMIN"
)

// User statuses
const (
	UserStatusInactive = "INACTIVE"
	UserStatusActive   = "ACTIVE"
	UserStatusBanned   = "BANNED"
)

// Article statuses
const (
	ArticleStatusDraft     = "DRAFT"
	ArticleStatusPublished = "PUBLISHED"
)

// Comment statuses
const (
	CommentStatusActive = "ACTIVE"
	CommentStatusHidden = "HIDDEN"
)

// Error codes and messages, aligned with the original Java ErrorCode enum.
const (
	CodeSuccess = 200

	CodeBadRequest      = 400
	CodeFileTooLarge    = 400
	CodeFileTypeInvalid = 400
	CodeWrongPassword   = 400
	CodeUnauthorized    = 401
	CodeRefreshBlack    = 401
	CodeForbidden       = 403
	CodeUserBanned      = 403
	CodeUserNotActivated = 403
	CodeNotFound        = 404
	CodeArticleNotFound = 404
	CodeCommentNotFound = 404
	CodeUserNotFound    = 404
	CodeConflict        = 409
	CodeUsernameExists  = 409
	CodeTagNameExists   = 409
	CodeTooManyReq      = 429

	CodeInternalError = 500
)

const (
	MsgOK                    = "ok"
	MsgFileTooLarge          = "文件大小超出限制"
	MsgFileTypeInvalid       = "文件类型不支持"
	MsgBadRequest            = "参数错误"
	MsgWrongPassword         = "密码错误"
	MsgWrongUserOrPassword   = "用户名或密码错误"
	MsgUnauthorized          = "未认证或Token已过期"
	MsgRefreshTokenBlack     = "RefreshToken已失效，请重新登录"
	MsgForbidden             = "无权限执行此操作"
	MsgUserBanned            = "用户已被封禁"
	MsgUserNotActivated      = "请先激活邮箱"
	MsgArticleNotFound       = "文章不存在"
	MsgCommentNotFound       = "评论不存在"
	MsgUserNotFound          = "用户不存在"
	MsgNotFound              = "资源不存在"
	MsgUsernameExists        = "用户名已存在"
	MsgTagNameExists         = "标签名已存在"
	MsgConflict              = "资源冲突"
	MsgTooManyRequests       = "请求过于频繁"
	MsgInternalError         = "服务器内部错误"
	MsgEmailAlreadyActivated = "该邮箱已激活"
	MsgVerifyLinkInvalid     = "激活链接已过期或无效"
)
