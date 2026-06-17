package jwt

import (
	"encoding/base64"
	"errors"
	"fmt"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
	"github.com/golang-jwt/jwt/v5"
)

// TokenType distinguishes access and refresh tokens.
type TokenType string

const (
	AccessToken  TokenType = "access"
	RefreshToken TokenType = "refresh"
)

// Claims is the custom JWT claims.
type Claims struct {
	UserID   int64  `json:"userId"`
	Username string `json:"username"`
	Role     string `json:"role"`
	jwt.RegisteredClaims
}

// Manager handles JWT operations.
type Manager struct {
	cfg    *config.JWTConfig
	secret []byte
}

// NewManager creates a JWT manager.
func NewManager(cfg *config.JWTConfig) (*Manager, error) {
	secret, err := base64.StdEncoding.DecodeString(cfg.Secret)
	if err != nil {
		return nil, fmt.Errorf("invalid jwt secret: %w", err)
	}
	return &Manager{cfg: cfg, secret: secret}, nil
}

// GenerateAccessToken creates a short-lived access token.
func (m *Manager) GenerateAccessToken(userID int64, username, role string) (string, error) {
	claims := Claims{
		UserID:   userID,
		Username: username,
		Role:     role,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject:   username,
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(m.cfg.AccessTokenExpiration)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(m.secret)
}

// GenerateRefreshToken creates a long-lived refresh token containing only userID.
func (m *Manager) GenerateRefreshToken(userID int64) (string, error) {
	claims := jwt.RegisteredClaims{
		Subject:   fmt.Sprintf("%d", userID),
		ExpiresAt: jwt.NewNumericDate(time.Now().Add(m.cfg.RefreshTokenExpiration)),
		IssuedAt:  jwt.NewNumericDate(time.Now()),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(m.secret)
}

// Validate verifies a token and returns its claims.
func (m *Manager) Validate(tokenString string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (any, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return m.secret, nil
	})
	if err != nil {
		return nil, err
	}
	if claims, ok := token.Claims.(*Claims); ok && token.Valid {
		return claims, nil
	}
	return nil, errors.New("invalid token claims")
}

// ValidateRefresh verifies a refresh token and returns the userID.
func (m *Manager) ValidateRefresh(tokenString string) (int64, error) {
	token, err := jwt.ParseWithClaims(tokenString, &jwt.RegisteredClaims{}, func(token *jwt.Token) (any, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return m.secret, nil
	})
	if err != nil {
		return 0, err
	}
	if claims, ok := token.Claims.(*jwt.RegisteredClaims); ok && token.Valid {
		var userID int64
		_, err := fmt.Sscanf(claims.Subject, "%d", &userID)
		return userID, err
	}
	return 0, errors.New("invalid refresh token claims")
}

// AccessExpirationSeconds returns access token expiration in seconds.
func (m *Manager) AccessExpirationSeconds() int64 {
	return int64(m.cfg.AccessTokenExpiration.Seconds())
}

// RefreshExpirationSeconds returns refresh token expiration in seconds.
func (m *Manager) RefreshExpirationSeconds() int64 {
	return int64(m.cfg.RefreshTokenExpiration.Seconds())
}
