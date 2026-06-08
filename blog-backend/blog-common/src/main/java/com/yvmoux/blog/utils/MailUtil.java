package com.yvmoux.blog.utils;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.yvmoux.blog.config.MailConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class MailUtil {

    private MailConfig mailConfig;

    public void sendMail() {
        Resend resend = new Resend(mailConfig.getApiKey());

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("yvmouX <noreply@mail.yvmoux.com>")
                .to("yvmou@outlook.com")
                //.cc()
                //.bcc()
                .replyTo("yvmou@outlook.com")
                .subject("Hello from Resend!")
                .text("This is a test email sent from Resend!")
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("发送成功: {}", data);
        } catch (ResendException e) {
            throw new RuntimeException(e);
        }
    }
}
