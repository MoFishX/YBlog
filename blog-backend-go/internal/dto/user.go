package dto

import "time"

// UpdateProfileRequest is the payload for updating user profile.
type UpdateProfileRequest struct {
	Email  string `json:"email" binding:"omitempty,email"`
	Avatar string `json:"avatar"`
}

// ChangePasswordRequest is the payload for changing password.
type ChangePasswordRequest struct {
	OldPassword string `json:"oldPassword" binding:"required"`
	NewPassword string `json:"newPassword" binding:"required,min=6,max=100"`
}

// UpdateUserStatusRequest is the payload for admin updating user status.
type UpdateUserStatusRequest struct {
	Status string `json:"status" binding:"required"`
}

// UpdateUserRoleRequest is the payload for admin updating user role.
type UpdateUserRoleRequest struct {
	Role string `json:"role" binding:"required"`
}

// AdminUserQuery holds query parameters for admin user list.
type AdminUserQuery struct {
	Page     int    `form:"page,default=1"`
	PageSize int    `form:"pageSize,default=20"`
	Keyword  string `form:"keyword"`
}

// UserVO is the user response.
type UserVO struct {
	ID           int64       `json:"id"`
	Username     string      `json:"username"`
	Email        string      `json:"email"`
	Avatar       string      `json:"avatar"`
	Role         string      `json:"role"`
	Articles     []ArticleVO `json:"articles,omitempty"`
	ArticleCount int         `json:"articleCount"`
	Status       string      `json:"status"`
	CreatedAt    time.Time   `json:"createdAt"`
}
