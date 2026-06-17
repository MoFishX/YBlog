package jwt

import (
	"testing"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
)

func TestJWT(t *testing.T) {
	cfg := &config.JWTConfig{
		Secret:                   "dGVzdC1zZWNyZXQta2V5LTEyMzQ1Njc4OTA=",
		AccessTokenExpiration:    time.Hour,
		RefreshTokenExpiration:   24 * time.Hour,
	}
	mgr, err := NewManager(cfg)
	if err != nil {
		t.Fatalf("new manager failed: %v", err)
	}

	access, err := mgr.GenerateAccessToken(1, "alice", "USER")
	if err != nil {
		t.Fatalf("generate access token failed: %v", err)
	}
	claims, err := mgr.Validate(access)
	if err != nil {
		t.Fatalf("validate access token failed: %v", err)
	}
	if claims.UserID != 1 || claims.Username != "alice" || claims.Role != "USER" {
		t.Fatalf("unexpected claims: %+v", claims)
	}

	refresh, err := mgr.GenerateRefreshToken(1)
	if err != nil {
		t.Fatalf("generate refresh token failed: %v", err)
	}
	userID, err := mgr.ValidateRefresh(refresh)
	if err != nil {
		t.Fatalf("validate refresh token failed: %v", err)
	}
	if userID != 1 {
		t.Fatalf("unexpected user id: %d", userID)
	}
}
