package com.novaleap.api.module.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.junit.jupiter.api.Test;

class SendEmailTest {

    @Test
    void sendEmailTest() {
        String apiKey = System.getenv("NOVA_RESEND_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("未配置 NOVA_RESEND_API_KEY，跳过发送测试");
            return;
        }

        String from = envOrDefault("NOVA_RESEND_FROM", "NovaLeap <zhiqi@novaleap.xyz>");
        String to = envOrDefault("NOVA_TEST_EMAIL_TO", "3247804029@qq.com");

        Resend resend = new Resend(apiKey.trim());

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .subject("NovaLeap验证码")
                .html("<strong>恭喜你，Resend 邮件发送成功</strong>")
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println(data.getId());
        } catch (ResendException e) {
            e.printStackTrace();
        }
    }

    private String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }
}

