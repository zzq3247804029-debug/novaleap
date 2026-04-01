package com.novaleap.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Ensure usernames are unique in existing data and enforce a unique index at DB level.
 * This prevents duplicate accounts under concurrent registration scenarios.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "nova.startup.username-guard", name = "enabled", havingValue = "true")
public class UserUsernameUniqGuard {

    private static final int USERNAME_MAX_LENGTH = 64;

    private final JdbcTemplate jdbcTemplate;

    public UserUsernameUniqGuard(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureUniqueUsernameConstraint() {
        try {
            if (!usersTableExists()) {
                return;
            }
            normalizeBlankOrTrimmedUsernames();
            deduplicateUsernames();
            ensureUniqueIndex();
        } catch (Exception e) {
            log.warn("ensure unique username guard failed: {}", e.getMessage());
        }
    }

    private boolean usersTableExists() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SHOW TABLES LIKE 'users'");
        return !rows.isEmpty();
    }

    private void normalizeBlankOrTrimmedUsernames() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, username FROM users ORDER BY id ASC");
        for (Map<String, Object> row : rows) {
            Long id = toLong(row.get("id"));
            if (id == null) {
                continue;
            }
            String current = safeTrim(toStr(row.get("username")));
            String next = StringUtils.hasText(current) ? truncate(current, USERNAME_MAX_LENGTH) : ("user_" + id);
            if (!next.equals(toStr(row.get("username")))) {
                jdbcTemplate.update("UPDATE users SET username = ? WHERE id = ?", next, id);
            }
        }
    }

    private void deduplicateUsernames() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, username, nickname FROM users ORDER BY id ASC"
        );

        Set<String> seen = new HashSet<>();
        int renamed = 0;

        for (Map<String, Object> row : rows) {
            Long id = toLong(row.get("id"));
            if (id == null) {
                continue;
            }
            String username = safeTrim(toStr(row.get("username")));
            if (!StringUtils.hasText(username)) {
                username = "user_" + id;
            }
            String uniqueKey = username.toLowerCase(Locale.ROOT);
            if (!seen.contains(uniqueKey) && !existsUsernameByOtherUser(username, id)) {
                seen.add(uniqueKey);
                continue;
            }

            String nextUsername = buildUniqueUsername(username, id, seen);
            String nickname = toStr(row.get("nickname"));
            jdbcTemplate.update(
                    "UPDATE users SET username = ?, nickname = CASE WHEN nickname IS NULL OR TRIM(nickname) = '' OR nickname = ? THEN ? ELSE nickname END WHERE id = ?",
                    nextUsername,
                    username,
                    nextUsername,
                    id
            );
            seen.add(nextUsername.toLowerCase(Locale.ROOT));
            renamed++;
        }

        if (renamed > 0) {
            log.warn("detected duplicate usernames and auto-renamed {} account(s)", renamed);
        }
    }

    private void ensureUniqueIndex() {
        List<Map<String, Object>> indexRows = jdbcTemplate.queryForList(
                "SHOW INDEX FROM users WHERE Column_name = 'username' AND Non_unique = 0"
        );
        if (!indexRows.isEmpty()) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE users ADD UNIQUE INDEX uk_users_username (username)");
        log.info("created unique index uk_users_username on users.username");
    }

    private String buildUniqueUsername(String baseUsername, Long id, Set<String> seen) {
        String base = StringUtils.hasText(baseUsername) ? baseUsername : "user";
        int seq = 0;
        while (seq < 10000) {
            String suffix = seq == 0 ? ("_" + id) : ("_" + id + "_" + seq);
            int maxBase = Math.max(1, USERNAME_MAX_LENGTH - suffix.length());
            String candidate = truncate(base, maxBase) + suffix;
            String key = candidate.toLowerCase(Locale.ROOT);
            if (!seen.contains(key) && !existsUsernameByOtherUser(candidate, id)) {
                return candidate;
            }
            seq++;
        }
        return "user_" + id + "_" + System.currentTimeMillis();
    }

    private boolean existsUsernameByOtherUser(String username, Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM users WHERE username = ? AND id <> ?",
                Integer.class,
                username,
                id
        );
        return count != null && count > 0;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String toStr(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String truncate(String value, int maxLen) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }
}
