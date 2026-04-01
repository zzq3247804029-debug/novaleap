package com.novaleap.api.module.auth.support;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
public class AuthRateLimitSupport {

    private static final int LOGIN_REQ_LIMIT_PER_MIN = 30;
    private static final Duration LOGIN_REQ_WINDOW = Duration.ofMinutes(1);
    private static final int LOGIN_FAIL_LOCK_AFTER = 5;
    private static final Duration LOGIN_FAIL_LOCK_TTL = Duration.ofMinutes(10);
    private static final int REGISTER_REQ_LIMIT_PER_HOUR = 20;
    private static final Duration REGISTER_REQ_WINDOW = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;

    public AuthRateLimitSupport(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String normalizeClientIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "unknown";
        }
        return ip.trim().replace(":", "_").replace("/", "_");
    }

    public void checkLoginRateLimit(String ipKey) {
        long bucket = System.currentTimeMillis() / 60000L;
        String key = "nova:auth:login:req:" + ipKey + ":" + bucket;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, LOGIN_REQ_WINDOW);
        }
        if (count != null && count > LOGIN_REQ_LIMIT_PER_MIN) {
            throw new IllegalArgumentException("操作过于频繁，请稍后再试");
        }
    }

    public void checkRegisterRateLimit(String ipKey) {
        long bucket = System.currentTimeMillis() / 3600000L;
        String key = "nova:auth:register:req:" + ipKey + ":" + bucket;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, REGISTER_REQ_WINDOW);
        }
        if (count != null && count > REGISTER_REQ_LIMIT_PER_HOUR) {
            throw new IllegalArgumentException("注册请求过于频繁，请稍后再试");
        }
    }

    public boolean isLoginLocked(String ipKey, String usernameKey) {
        String key = loginFailKey(ipKey, usernameKey);
        String val = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(val)) {
            return false;
        }
        try {
            return Long.parseLong(val) >= LOGIN_FAIL_LOCK_AFTER;
        } catch (Exception ignore) {
            return false;
        }
    }

    public void recordLoginFailure(String ipKey, String usernameKey) {
        String key = loginFailKey(ipKey, usernameKey);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, LOGIN_FAIL_LOCK_TTL);
        }
    }

    public void clearLoginFailures(String ipKey, String usernameKey) {
        redisTemplate.delete(loginFailKey(ipKey, usernameKey));
    }

    private String loginFailKey(String ipKey, String usernameKey) {
        return "nova:auth:login:fail:" + ipKey + ":" + usernameKey;
    }
}
