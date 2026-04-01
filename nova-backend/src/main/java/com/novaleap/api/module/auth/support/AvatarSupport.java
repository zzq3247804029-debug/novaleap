package com.novaleap.api.module.auth.support;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class AvatarSupport {

    private static final String PROFILE_AVATAR_KEY_PREFIX = "nova:profile:avatar:";
    private static final String DEFAULT_AVATAR = "🥳";
    private static final Set<String> ALLOWED_AVATARS = Set.of(
            "🥳", "😃", "🤗", "😅", "😉",
            "🫡", "😊", "😏", "🤩", "😎",
            "😁", "🥸", "🧐", "🙅", "😆",
            "😺", "😈", "😌", "🥰", "🤓",
            "😒", "😍", "🥹", "😷", "🤔",
            "😄", "😀", "😋", "🙌", "😇"
    );

    private final StringRedisTemplate redisTemplate;

    public AvatarSupport(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String resolveAvatar(String username) {
        String key = PROFILE_AVATAR_KEY_PREFIX + username;
        String avatar = normalizeAvatarInput(redisTemplate.opsForValue().get(key));
        if (!StringUtils.hasText(avatar) || !ALLOWED_AVATARS.contains(avatar)) {
            // 修复历史脏数据（包含乱码头像），统一回落为默认头像。
            redisTemplate.opsForValue().set(key, DEFAULT_AVATAR);
            return DEFAULT_AVATAR;
        }
        return avatar;
    }

    public String normalizeAvatarInput(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            return "";
        }
        return avatar.trim().replace("\uFE0F", "").replace("\u200D", "");
    }

    public boolean isAllowedAvatar(String avatar) {
        return ALLOWED_AVATARS.contains(avatar);
    }

    public void saveAvatar(String username, String avatar) {
        redisTemplate.opsForValue().set(PROFILE_AVATAR_KEY_PREFIX + username, avatar);
    }
}
