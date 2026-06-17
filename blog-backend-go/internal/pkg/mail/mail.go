package mail

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/http"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
)

// Sender sends emails via the Resend API.
type Sender struct {
	apiKey string
	client *http.Client
}

// NewSender creates a new Resend email sender.
func NewSender(cfg *config.EmailConfig) *Sender {
	return &Sender{
		apiKey: cfg.ResendAPIKey,
		client: &http.Client{Timeout: 30 * time.Second},
	}
}

// SendHTML sends an HTML email. If the API key is empty, it logs and skips.
func (s *Sender) SendHTML(from, to, subject, html string) {
	if s.apiKey == "" {
		slog.Warn("resend api key not configured, skipping email", slog.String("to", to), slog.String("subject", subject))
		return
	}

	body, _ := json.Marshal(map[string]string{
		"from":    from,
		"to":      to,
		"subject": subject,
		"html":    html,
	})

	req, err := http.NewRequest(http.MethodPost, "https://api.resend.com/emails", bytes.NewReader(body))
	if err != nil {
		slog.Error("failed to build email request", slog.String("error", err.Error()))
		return
	}
	req.Header.Set("Authorization", "Bearer "+s.apiKey)
	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		slog.Error("failed to send email", slog.String("to", to), slog.String("error", err.Error()))
		return
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		slog.Error("resend api returned error", slog.Int("status", resp.StatusCode), slog.String("to", to))
		return
	}
	slog.Info("email sent", slog.String("to", to), slog.String("subject", subject))
}

// BuildActivationEmail builds the HTML body for activation emails.
func BuildActivationEmail(verifyURL string) string {
	return fmt.Sprintf(`<p>点击以下链接激活你的博客账号：</p>
<p><a href="%s">%s</a></p>
<p>链接 24 小时内有效。</p>`, verifyURL, verifyURL)
}
