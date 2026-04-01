package com.novaleap.api.task;

import com.novaleap.api.entity.Wish;
import com.novaleap.api.module.ai.support.AiModelGateway;
import com.novaleap.api.module.ai.support.AiPromptFactory;
import com.novaleap.api.module.wish.support.WishQueueConstants;
import com.novaleap.api.service.AiLimitService;
import com.novaleap.api.service.WishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableScheduling
public class WishConsumerTask {

    private static final int MAX_BATCH_SIZE = 5;
    private static final int MAX_SCAN_SIZE = 50;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long PROCESSING_LOCK_TTL_SECONDS = 120L;
    private static final Duration RETRY_TTL = Duration.ofDays(1);

    private final StringRedisTemplate redisTemplate;
    private final WishService wishService;
    private final AiModelGateway aiModelGateway;
    private final AiPromptFactory aiPromptFactory;

    public WishConsumerTask(
            StringRedisTemplate redisTemplate,
            WishService wishService,
            AiModelGateway aiModelGateway,
            AiPromptFactory aiPromptFactory
    ) {
        this.redisTemplate = redisTemplate;
        this.wishService = wishService;
        this.aiModelGateway = aiModelGateway;
        this.aiPromptFactory = aiPromptFactory;
    }

    @Scheduled(fixedDelay = 3000)
    public void processPendingWishes() {
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            if (!processNextWish()) {
                return;
            }
        }
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 15000)
    public void recoverStaleProcessingWishes() {
        List<String> processingWishIds = redisTemplate.opsForList().range(WishQueueConstants.PROCESSING_QUEUE, 0, MAX_SCAN_SIZE - 1);
        if (processingWishIds == null || processingWishIds.isEmpty()) {
            return;
        }
        for (String wishIdStr : processingWishIds) {
            if (!StringUtils.hasText(wishIdStr)) {
                removeFromProcessingQueue(wishIdStr);
                continue;
            }
            if (hasProcessingLock(wishIdStr)) {
                continue;
            }
            removeFromProcessingQueue(wishIdStr);
            Long wishId = parseWishId(wishIdStr);
            if (wishId == null || isDeadLetter(wishIdStr)) {
                continue;
            }
            if (wishService.getPendingWish(wishId) != null) {
                wishService.enqueuePendingWish(wishId);
            }
        }
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 20000)
    public void requeuePendingWishes() {
        Set<String> queuedWishIds = new HashSet<>();
        List<String> pendingWishIds = redisTemplate.opsForList().range(WishQueueConstants.PENDING_QUEUE, 0, MAX_SCAN_SIZE - 1);
        if (pendingWishIds != null) {
            queuedWishIds.addAll(pendingWishIds);
        }
        List<String> processingWishIds = redisTemplate.opsForList().range(WishQueueConstants.PROCESSING_QUEUE, 0, MAX_SCAN_SIZE - 1);
        if (processingWishIds != null) {
            queuedWishIds.addAll(processingWishIds);
        }

        for (Long wishId : wishService.listPendingWishIds(MAX_SCAN_SIZE)) {
            String wishIdStr = String.valueOf(wishId);
            if (queuedWishIds.contains(wishIdStr) || hasProcessingLock(wishIdStr) || isDeadLetter(wishIdStr)) {
                continue;
            }
            wishService.enqueuePendingWish(wishId);
        }
    }

    private boolean processNextWish() {
        String wishIdStr = redisTemplate.opsForList().rightPopAndLeftPush(WishQueueConstants.PENDING_QUEUE, WishQueueConstants.PROCESSING_QUEUE);
        if (!StringUtils.hasText(wishIdStr)) {
            return false;
        }

        Long wishId = parseWishId(wishIdStr);
        if (wishId == null) {
            removeFromProcessingQueue(wishIdStr);
            return true;
        }
        if (isDeadLetter(wishIdStr)) {
            removeFromProcessingQueue(wishIdStr);
            return true;
        }
        if (!acquireProcessingLock(wishIdStr)) {
            removeFromProcessingQueue(wishIdStr);
            return true;
        }

        try {
            Wish wish = wishService.getPendingWish(wishId);
            if (wish == null) {
                removeFromProcessingQueue(wishIdStr);
                clearRetryState(wishIdStr);
                return true;
            }

            EmotionProfile profile = analyzeWish(wish.getContent());
            int posX = ThreadLocalRandom.current().nextInt(10, 91);
            int posY = ThreadLocalRandom.current().nextInt(10, 91);

            boolean updated = wishService.completeAutoReview(
                    wishId,
                    profile.emotion(),
                    profile.color(),
                    profile.speed(),
                    posX,
                    posY
            );
            removeFromProcessingQueue(wishIdStr);
            clearRetryState(wishIdStr);
            log.info("wish auto review completed, wishId={}, updated={}, emotion={}", wishId, updated, profile.emotion());
            return true;
        } catch (Exception e) {
            handleProcessingFailure(wishIdStr, e);
            return true;
        } finally {
            releaseProcessingLock(wishIdStr);
        }
    }

    private EmotionProfile analyzeWish(String content) {
        String prompt = aiPromptFactory.buildWishEmotionPrompt(content);
        String raw = aiModelGateway.callModel(
                "",
                prompt,
                "hopeful",
                AiLimitService.AiModule.CHAT
        );
        String emotion = normalizeEmotion(raw);
        return switch (emotion) {
            case "happy" -> new EmotionProfile("happy", "#D4E0D0", 1.6D);
            case "confused" -> new EmotionProfile("confused", "#B8C4D4", 0.8D);
            case "anxious" -> new EmotionProfile("anxious", "#E0D3C1", 1.8D);
            default -> new EmotionProfile("hopeful", "#DDBFD1", 1.2D);
        };
    }

    private String normalizeEmotion(String raw) {
        String value = safe(raw).toLowerCase(Locale.ROOT);
        if (value.contains("happy")) {
            return "happy";
        }
        if (value.contains("confused")) {
            return "confused";
        }
        if (value.contains("anxious")) {
            return "anxious";
        }
        return "hopeful";
    }

    private void handleProcessingFailure(String wishIdStr, Exception e) {
        removeFromProcessingQueue(wishIdStr);
        long retryCount = incrementRetryCount(wishIdStr);
        if (retryCount >= MAX_RETRY_COUNT) {
            redisTemplate.opsForSet().add(WishQueueConstants.DEAD_LETTER_SET, wishIdStr);
            log.error("wish auto review moved to dead letter, wishId={}, retryCount={}", wishIdStr, retryCount, e);
            return;
        }
        redisTemplate.opsForList().rightPush(WishQueueConstants.PENDING_QUEUE, wishIdStr);
        log.warn("wish auto review failed, requeued wishId={}, retryCount={}", wishIdStr, retryCount, e);
    }

    private long incrementRetryCount(String wishIdStr) {
        Long retryCount = redisTemplate.opsForValue().increment(WishQueueConstants.RETRY_PREFIX + wishIdStr);
        redisTemplate.expire(WishQueueConstants.RETRY_PREFIX + wishIdStr, RETRY_TTL);
        return retryCount == null ? 0L : retryCount;
    }

    private void clearRetryState(String wishIdStr) {
        redisTemplate.delete(WishQueueConstants.RETRY_PREFIX + wishIdStr);
        redisTemplate.opsForSet().remove(WishQueueConstants.DEAD_LETTER_SET, wishIdStr);
    }

    private boolean acquireProcessingLock(String wishIdStr) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                WishQueueConstants.PROCESSING_LOCK_PREFIX + wishIdStr,
                "1",
                PROCESSING_LOCK_TTL_SECONDS,
                TimeUnit.SECONDS
        );
        return Boolean.TRUE.equals(locked);
    }

    private boolean hasProcessingLock(String wishIdStr) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(WishQueueConstants.PROCESSING_LOCK_PREFIX + wishIdStr));
    }

    private void releaseProcessingLock(String wishIdStr) {
        redisTemplate.delete(WishQueueConstants.PROCESSING_LOCK_PREFIX + wishIdStr);
    }

    private void removeFromProcessingQueue(String wishIdStr) {
        if (!StringUtils.hasText(wishIdStr)) {
            return;
        }
        redisTemplate.opsForList().remove(WishQueueConstants.PROCESSING_QUEUE, 1, wishIdStr);
    }

    private boolean isDeadLetter(String wishIdStr) {
        Boolean member = redisTemplate.opsForSet().isMember(WishQueueConstants.DEAD_LETTER_SET, wishIdStr);
        return Boolean.TRUE.equals(member);
    }

    private Long parseWishId(String wishIdStr) {
        try {
            return Long.parseLong(wishIdStr);
        } catch (Exception e) {
            log.warn("invalid wish id in queue: {}", wishIdStr);
            return null;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record EmotionProfile(String emotion, String color, double speed) {
    }
}
