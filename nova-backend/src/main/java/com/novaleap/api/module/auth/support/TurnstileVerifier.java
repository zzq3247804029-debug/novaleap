package com.novaleap.api.module.auth.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Service
public class TurnstileVerifier {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String secret;

    public TurnstileVerifier(
            ObjectMapper objectMapper,
            @Value("${nova.turnstile.enabled:false}") boolean enabled,
            @Value("${nova.turnstile.secret:}") String secret
    ) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.secret = secret == null ? "" : secret.trim();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    public void verifyIfEnabled(String token, String ip) {
        if (!enabled || !StringUtils.hasText(secret)) {
            return;
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("请先完成验证码校验");
        }

        try {
            String payload =
                    "secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8)
                            + "&response=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                            + "&remoteip=" + URLEncoder.encode(ip == null ? "" : ip, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://challenges.cloudflare.com/turnstile/v0/siteverify"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            if (!Boolean.TRUE.equals(body.get("success"))) {
                throw new IllegalArgumentException("验证码校验失败，请重试");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("验证码校验失败，请重试");
        }
    }
}
