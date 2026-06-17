package service

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/http"
	"time"

	"github.com/MoFishX/YBlog/blog-backend-go/internal/config"
)

const (
	systemPromptSummaryLong = `你是一个技术文章编辑。请通读以下文章全文，生成一份结构化的中文总结。

要求：
1. 使用 Markdown 格式组织输出
2. 必须包含以下模块：
   - ## 核心观点（2-3句话概括文章主旨）
   - ## 关键要点（3-5个要点，每个一行）
   - ## 技术关键词（逗号分隔）
3. 如果文章包含代码示例，简要说明其作用
4. 总字数 300-800 字，禁止废话
5. 基于原文事实，不要编造
`
	systemPromptSummary = `你是一个技术文章编辑。请通读以下文章全文，生成一份简短的文章摘要。

要求：
1. 使用 纯文本组织输出，禁止使用 MarkDown 格式
2. 总字数 50-100 字，禁止废话
3. 基于原文事实，不要编造
4. 尽可能的在概括原文的基础上吸引读者眼球
`
)

// AIService calls the configured LLM for summarization.
type AIService struct {
	cfg    *config.AIConfig
	client *http.Client
}

// NewAIService creates an AIService.
func NewAIService(cfg *config.AIConfig) *AIService {
	return &AIService{
		cfg:    cfg,
		client: &http.Client{Timeout: 120 * time.Second},
	}
}

// Summarize generates a short summary.
func (s *AIService) Summarize(title, content string) string {
	return s.call(title, content, systemPromptSummary)
}

// SummarizeLong generates a long structured summary.
func (s *AIService) SummarizeLong(title, content string) string {
	return s.call(title, content, systemPromptSummaryLong)
}

func (s *AIService) call(title, content, systemPrompt string) string {
	if !s.cfg.Enabled {
		slog.Warn("ai summarization is disabled")
		return ""
	}

	reqBody := map[string]any{
		"model": s.cfg.Model,
		"messages": []map[string]string{
			{"role": "system", "content": systemPrompt},
			{"role": "user", "content": "文章标题：" + title + "\n\n文章内容：\n" + content},
		},
		"max_tokens":  s.cfg.MaxTokens,
		"temperature": s.cfg.Temperature,
	}
	body, _ := json.Marshal(reqBody)

	slog.Info("calling ai summary", slog.Int("contentLength", len(content)))
	req, err := http.NewRequest(http.MethodPost, s.cfg.BaseURL+"/v1/chat/completions", bytes.NewReader(body))
	if err != nil {
		slog.Error("failed to build ai request", slog.String("error", err.Error()))
		return ""
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+s.cfg.APIKey)

	resp, err := s.client.Do(req)
	if err != nil {
		slog.Error("ai request failed", slog.String("error", err.Error()))
		return ""
	}
	defer resp.Body.Close()

	var chatResp chatCompletionResponse
	if err := json.NewDecoder(resp.Body).Decode(&chatResp); err != nil {
		slog.Error("failed to decode ai response", slog.String("error", err.Error()))
		return ""
	}
	if len(chatResp.Choices) > 0 && chatResp.Choices[0].Message.Content != "" {
		summary := chatResp.Choices[0].Message.Content
		slog.Info("ai summary completed", slog.Int("summaryLength", len(summary)))
		return summary
	}
	slog.Warn("ai returned empty result")
	return ""
}

type chatCompletionResponse struct {
	Choices []struct {
		Message struct {
			Content string `json:"content"`
		} `json:"message"`
	} `json:"choices"`
}

// Helper kept for formatting consistency.
func (s *AIService) formatUserMessage(title, content string) string {
	return fmt.Sprintf("文章标题：%s\n\n文章内容：\n%s", title, content)
}
