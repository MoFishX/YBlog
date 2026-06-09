package com.yvmoux.blog.utils;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.yvmoux.blog.config.MailConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailUtil {

    private final MailConfig mailConfig;

    public void sendHtml(String from, String to, String subject, String html) {
        if (mailConfig.getApiKey() == null || mailConfig.getApiKey().isBlank()) {
            log.warn("Resend API Key 未配置，跳过邮件发送。收件人: {}, 主题: {}", to, subject);
            return;
        }
        try {
            Resend resend = new Resend(mailConfig.getApiKey());
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            CreateEmailResponse data = resend.emails().send(params);
            log.info("邮件发送成功: {}", data);
        } catch (ResendException e) {
            log.error("邮件发送失败, to: {}", to, e);
        }
    }
}
