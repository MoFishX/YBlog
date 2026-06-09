package com.yvmoux.blog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yvmoux.blog.config.AIConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final AIConfig aiConfig;

    private static final String SYSTEM_PROMPT_AI_SUMMARY_LONG = """
            你是一个技术文章编辑。请通读以下文章全文，生成一份结构化的中文总结。
            
            要求：
            1. 使用 Markdown 格式组织输出
            2. 必须包含以下模块：
               - ## 核心观点（2-3句话概括文章主旨）
               - ## 关键要点（3-5个要点，每个一行）
               - ## 技术关键词（逗号分隔）
            3. 如果文章包含代码示例，简要说明其作用
            4. 总字数 300-800 字，禁止废话
            5. 基于原文事实，不要编造
            """;

    private static final String SYSTEM_PROMPT_AI_SUMMARY = """
            你是一个技术文章编辑。请通读以下文章全文，生成一份简短的文章摘要。
            
            要求：
            1. 使用 纯文本组织输出，禁止使用 MarkDown 格式
            2. 总字数 50-100 字，禁止废话
            3. 基于原文事实，不要编造
            4. 尽可能的在概括原文的基础上吸引读者眼球
            """;

    public String summarize(String title, String content) {
        return base(title, content, SYSTEM_PROMPT_AI_SUMMARY);
    }
    public String summarizeLong(String title, String content) {
        return base(title, content, SYSTEM_PROMPT_AI_SUMMARY_LONG);
    }

    private String base(String title, String content, String systemPrompt) {
        if (!aiConfig.isEnabled()) {
            log.warn("AI summarization is disabled");
            return null;
        }

        String userMessage = "文章标题：" + title + "\n\n文章内容：\n" + content;


        Map<String, Object> requestBody = Map.of(
                "model", aiConfig.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", aiConfig.getMaxTokens(),
                "temperature", aiConfig.getTemperature()
        );

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String json = objectMapper.writeValueAsString(requestBody);
            log.info("调用 AI 总结，文章长度: {} 字符", content.length());

            String response = RestClient.create()
                    .post()
                    .uri(aiConfig.getBaseUrl() + "/v1/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .body(json)
                    .retrieve()
                    .body(String.class);

            // 解析响应
            ChatCompletionResponse res = objectMapper.readValue(response, ChatCompletionResponse.class);
            if (res.getChoices() != null && !res.getChoices().isEmpty()) {
                String summary = res.getChoices().getFirst().getMessage().getContent();
                log.info("AI 总结完成，长度: {} 字符", summary.length());
                return summary;
            }

            log.warn("AI 返回空结果: {}", response);
            return null;
        } catch (JsonProcessingException e) {
            log.error("AI 总结调用失败", e);
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionResponse {
        private List<Choice> choices;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {
            private Message message;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            private String content;
        }
    }
}
