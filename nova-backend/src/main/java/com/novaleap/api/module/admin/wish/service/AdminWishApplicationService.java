package com.novaleap.api.module.admin.wish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.exception.NotFoundException;
import com.novaleap.api.entity.Wish;
import com.novaleap.api.mapper.WishMapper;
import com.novaleap.api.module.admin.wish.assembler.AdminWishViewAssembler;
import com.novaleap.api.module.admin.wish.dto.AdminWishSaveRequest;
import com.novaleap.api.module.admin.wish.dto.AdminWishStatusRequest;
import com.novaleap.api.module.admin.wish.vo.AdminWishQueueStatsVO;
import com.novaleap.api.module.admin.wish.vo.AdminWishVO;
import com.novaleap.api.module.wish.support.WishQueueConstants;
import com.novaleap.api.service.WishService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminWishApplicationService {

    private static final int WISH_STATUS_PENDING = 0;
    private static final int WISH_STATUS_APPROVED = 1;
    private static final int WISH_STATUS_REJECTED = 2;
    private static final int MAX_DEAD_LETTER_SIZE = 20;

    private final WishMapper wishMapper;
    private final WishService wishService;
    private final StringRedisTemplate redisTemplate;

    public AdminWishApplicationService(
            WishMapper wishMapper,
            WishService wishService,
            StringRedisTemplate redisTemplate
    ) {
        this.wishMapper = wishMapper;
        this.wishService = wishService;
        this.redisTemplate = redisTemplate;
    }

    public Page<AdminWishVO> getWishList(Integer page, Integer size, Integer status) {
        Page<Wish> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Wish> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Wish::getStatus, status);
        }
        wrapper.orderByDesc(Wish::getCreatedAt);

        Page<Wish> result = wishMapper.selectPage(pageParam, wrapper);
        return toWishPage(result);
    }

    public AdminWishVO getWishDetail(Long id) {
        return AdminWishViewAssembler.toVO(loadWish(id));
    }

    public AdminWishQueueStatsVO getWishQueueStats() {
        AdminWishQueueStatsVO vo = new AdminWishQueueStatsVO();
        vo.setPendingQueueSize(safeSize(redisTemplate.opsForList().size(WishQueueConstants.PENDING_QUEUE)));
        vo.setProcessingQueueSize(safeSize(redisTemplate.opsForList().size(WishQueueConstants.PROCESSING_QUEUE)));
        vo.setDeadLetterSize(safeSize(redisTemplate.opsForSet().size(WishQueueConstants.DEAD_LETTER_SET)));
        vo.setPendingDbCount(safeSize(
                wishMapper.selectCount(new LambdaQueryWrapper<Wish>().eq(Wish::getStatus, WISH_STATUS_PENDING))
        ));
        return vo;
    }

    public List<AdminWishVO> getDeadLetterWishes(Integer size) {
        int limit = Math.max(1, Math.min(size == null ? MAX_DEAD_LETTER_SIZE : size, MAX_DEAD_LETTER_SIZE));
        Set<String> rawWishIds = redisTemplate.opsForSet().members(WishQueueConstants.DEAD_LETTER_SET);
        if (rawWishIds == null || rawWishIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> wishIds = rawWishIds.stream()
                .map(this::parseWishId)
                .filter(Objects::nonNull)
                .limit(limit)
                .toList();
        if (wishIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Wish> wishes = wishMapper.selectBatchIds(wishIds);
        if (wishes == null || wishes.isEmpty()) {
            return Collections.emptyList();
        }
        return wishes.stream()
                .sorted(Comparator.comparing(Wish::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(AdminWishViewAssembler::toVO)
                .toList();
    }

    public AdminWishVO createWish(AdminWishSaveRequest request) {
        String content = trim(request.getContent());
        if (isBlank(content)) {
            throw new IllegalArgumentException("愿望内容不能为空");
        }

        Wish wish = new Wish();
        wish.setContent(content);
        wish.setEmotion(defaultText(request.getEmotion()));
        wish.setColor(defaultText(request.getColor()));
        wish.setCity(defaultText(request.getCity()));
        wish.setPosX(request.getPosX());
        wish.setPosY(request.getPosY());
        wish.setFloatSpeed(request.getFloatSpeed());

        Integer status = request.getStatus() == null ? WISH_STATUS_PENDING : normalizeWishStatus(request.getStatus());
        if (status == null) {
            throw new IllegalArgumentException("愿望状态不合法");
        }
        wish.setStatus(status);
        wish.setCreatedAt(LocalDateTime.now());
        wishMapper.insert(wish);
        return AdminWishViewAssembler.toVO(wish);
    }

    public AdminWishVO updateWish(Long id, AdminWishSaveRequest request) {
        Wish wish = loadWish(id);

        String content = trim(request.getContent());
        Integer status = normalizeWishStatus(request.getStatus());
        if (!isBlank(content)) {
            wish.setContent(content);
        }
        if (request.getEmotion() != null) {
            wish.setEmotion(defaultText(request.getEmotion()));
        }
        if (request.getColor() != null) {
            wish.setColor(defaultText(request.getColor()));
        }
        if (request.getCity() != null) {
            wish.setCity(defaultText(request.getCity()));
        }
        if (request.getPosX() != null) {
            wish.setPosX(request.getPosX());
        }
        if (request.getPosY() != null) {
            wish.setPosY(request.getPosY());
        }
        if (request.getFloatSpeed() != null) {
            wish.setFloatSpeed(request.getFloatSpeed());
        }
        if (request.getStatus() != null) {
            if (status == null) {
                throw new IllegalArgumentException("愿望状态不合法");
            }
            wish.setStatus(status);
        }

        wishMapper.updateById(wish);
        return AdminWishViewAssembler.toVO(wish);
    }

    public void deleteWish(Long id) {
        loadWish(id);
        wishMapper.deleteById(id);
    }

    public void updateWishStatus(Long id, Integer status, AdminWishStatusRequest body) {
        Wish wish = loadWish(id);
        Integer requestedStatus = status != null ? status : body == null ? null : body.getStatus();
        Integer normalized = normalizeWishStatus(requestedStatus);
        if (normalized == null) {
            throw new IllegalArgumentException("愿望状态不合法");
        }
        wish.setStatus(normalized);
        wishMapper.updateById(wish);
    }

    public void retryWishReview(Long id) {
        Wish wish = loadWish(id);
        if (!Objects.equals(wish.getStatus(), WISH_STATUS_PENDING)) {
            throw new IllegalArgumentException("仅待审核的愿望可以重试");
        }

        String wishIdStr = String.valueOf(id);
        redisTemplate.opsForSet().remove(WishQueueConstants.DEAD_LETTER_SET, wishIdStr);
        redisTemplate.delete(WishQueueConstants.RETRY_PREFIX + wishIdStr);
        redisTemplate.delete(WishQueueConstants.PROCESSING_LOCK_PREFIX + wishIdStr);
        redisTemplate.opsForList().remove(WishQueueConstants.PROCESSING_QUEUE, 0, wishIdStr);
        removeDuplicatePendingEntries(wishIdStr);
        wishService.enqueuePendingWish(id);
    }

    private Wish loadWish(Long id) {
        Wish wish = wishMapper.selectById(id);
        if (wish == null) {
            throw new NotFoundException("愿望不存在");
        }
        return wish;
    }

    private Page<AdminWishVO> toWishPage(Page<Wish> sourcePage) {
        Page<AdminWishVO> targetPage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        List<AdminWishVO> records = sourcePage.getRecords() == null
                ? Collections.emptyList()
                : sourcePage.getRecords().stream().map(AdminWishViewAssembler::toVO).toList();
        targetPage.setRecords(records);
        return targetPage;
    }

    private Integer normalizeWishStatus(Integer status) {
        if (status == null) {
            return null;
        }
        if (status != WISH_STATUS_PENDING && status != WISH_STATUS_APPROVED && status != WISH_STATUS_REJECTED) {
            return null;
        }
        return status;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private long safeSize(Long value) {
        return value == null ? 0L : value;
    }

    private Long parseWishId(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void removeDuplicatePendingEntries(String wishIdStr) {
        redisTemplate.opsForList().remove(WishQueueConstants.PENDING_QUEUE, 0, wishIdStr);
    }
}
