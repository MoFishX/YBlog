package dto

// LoginRequest is the login payload.
type LoginRequest struct {
	Username   string `json:"username" binding:"required,min=2,max=50"`
	Password   string `json:"password" binding:"required,min=6,max=100"`
	RememberMe bool   `json:"rememberMe"`
}

// RegisterRequest is the registration payload.
type RegisterRequest struct {
	Username string `json:"username" binding:"required,min=2,max=50"`
	Password string `json:"password" binding:"required,min=6,max=100"`
	Email    string `json:"email" binding:"omitempty,email"`
}

// RefreshTokenQuery holds the refresh token form value.
type RefreshTokenQuery struct {
	RefreshToken string `form:"refreshToken"`
}

// VerifyEmailQuery holds the email verification token.
type VerifyEmailQuery struct {
	Token string `form:"token" binding:"required"`
}

// ResendActivationQuery holds the email for resending activation.
type ResendActivationQuery struct {
	Email string `form:"email" binding:"required,email"`
}

// LoginResult is returned on successful login.
type LoginResult struct {
	AccessToken  string  `json:"accessToken"`
	RefreshToken string  `json:"refreshToken"`
	ExpiresIn    int64   `json:"expiresIn"`
	User         LoginVO `json:"user"`
}

// RefreshResult is returned on token refresh.
type RefreshResult struct {
	AccessToken string  `json:"accessToken"`
	ExpiresIn   int64   `json:"expiresIn"`
	User        LoginVO `json:"user"`
}

// LoginVO is the user info embedded in login/refresh responses.
type LoginVO struct {
	ID       int64  `json:"id"`
	Username string `json:"username"`
	Email    string `json:"email"`
	Avatar   string `json:"avatar"`
	Role     string `json:"role"`
	Status   string `json:"status"`
}
