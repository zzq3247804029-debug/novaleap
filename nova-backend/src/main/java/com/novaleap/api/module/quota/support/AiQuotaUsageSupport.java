package com.novaleap.api.module.quota.support;

import com.novaleap.api.service.AiLimitService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class AiQuotaUsageSupport {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String KEY_TOTAL = "ai:usage:total:";
    private static final String KEY_MODULE = "ai:usage:module:";
    private static final String KEY_TOKENS = "ai:usage:tokens:global:";
    private static final String KEY_COOLDOWN = "ai:usage:cooldown:";

    private final StringRedisTemplate redisTemplate;

    public AiQuotaUsageSupport(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean inCooldown(String identifier, AiLimitService.AiModule module) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_COOLDOWN + identifier + ":" + module));
    }

    public int currentTotal(String identifier) {
        return parseInt(redisTemplate.opsForValue().get(KEY_TOTAL + identifier + ":" + today()));
    }

    public int currentModule(String identifier, AiLimitService.AiModule module) {
        return parseInt(redisTemplate.opsForValue().get(KEY_MODULE + module.name().toLowerCase() + ":" + identifier + ":" + today()));
    }

    public void incrementUsage(String identifier, AiLimitService.AiModule module, int cooldownSeconds) {
        String totalKey = KEY_TOTAL + identifier + ":" + today();
        redisTemplate.opsForValue().increment(totalKey);
        redisTemplate.expire(totalKey, Duration.ofDays(1));

        String moduleKey = KEY_MODULE + module.name().toLowerCase() + ":" + identifier + ":" + today();
        redisTemplate.opsForValue().increment(moduleKey);
        redisTemplate.expire(moduleKey, Duration.ofDays(1));

        redisTemplate.opsForValue().set(KEY_COOLDOWN + identifier + ":" + module, "1", cooldownSeconds, TimeUnit.SECONDS);
    }

    public void recordTokenUsage(long tokens) {
        if (tokens <= 0) {
            return;
        }
        String key = KEY_TOKENS + today();
        redisTemplate.opsForValue().increment(key, tokens);
        redisTemplate.expire(key, Duration.ofDays(2));
    }

    public long currentTokenUsage() {
        return parseLong(redisTemplate.opsForValue().get(KEY_TOKENS + today()));
    }

    private String today() {
        return LocalDate.now().format(DATE_FMT);
    }

    private int parseInt(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ignore) {
            return 0;
        }
    }

    private long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception ignore) {
            return 0L;
        }
    }
}