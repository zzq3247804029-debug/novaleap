package com.novaleap.api.module.auth.support;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthCheckinSupport {

    private static final String CHECKIN_BITMAP_KEY_PREFIX = "nova:checkin:";
    private static final ZoneId CHECKIN_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter MONTH_KEY_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    private final StringRedisTemplate redisTemplate;

    public AuthCheckinSupport(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Object> resolveCheckinPayload(Long userId, boolean markToday) {
        if (userId == null || userId <= 0) {
            return buildCheckinPayload(0, false);
        }
        try {
            LocalDate today = LocalDate.now(CHECKIN_ZONE);
            if (markToday) {
                markSignedToday(userId, today);
            }
            boolean signedToday = isSigned(userId, today);
            int streakDays = calcConsecutiveDays(userId, today);
            return buildCheckinPayload(streakDays, signedToday);
        } catch (Exception ignore) {
            return buildCheckinPayload(0, false);
        }
    }

    private Map<String, Object> buildCheckinPayload(int streakDays, boolean signedToday) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("streakDays", Math.max(0, streakDays));
        payload.put("signedToday", signedToday);
        return payload;
    }

    private void markSignedToday(Long userId, LocalDate day) {
        String key = checkinMonthKey(userId, day);
        int bitIndex = day.getDayOfMonth() - 1;
        redisTemplate.opsForValue().setBit(key, bitIndex, true);
        redisTemplate.expire(key, Duration.ofDays(400));
    }

    private boolean isSigned(Long userId, LocalDate day) {
        String key = checkinMonthKey(userId, day);
        int bitIndex = day.getDayOfMonth() - 1;
        Boolean bit = redisTemplate.opsForValue().getBit(key, bitIndex);
        return Boolean.TRUE.equals(bit);
    }

    private int calcConsecutiveDays(Long userId, LocalDate today) {
        int streak = 0;
        LocalDate cursor = today;
        for (int i = 0; i < 3660; i++) {
            if (!isSigned(userId, cursor)) {
                break;
            }
            streak += 1;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private String checkinMonthKey(Long userId, LocalDate day) {
        return CHECKIN_BITMAP_KEY_PREFIX + userId + ":" + MONTH_KEY_FMT.format(day);
    }
}