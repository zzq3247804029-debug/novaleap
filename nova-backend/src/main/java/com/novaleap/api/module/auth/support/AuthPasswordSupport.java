package com.novaleap.api.module.auth.support;

import com.novaleap.api.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class AuthPasswordSupport {

    private static final int PASSWORD_MIN_LEN = 6;
    private static final int PASSWORD_MAX_LEN = 20;

    private final PasswordEncoder passwordEncoder;

    public AuthPasswordSupport(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public String normalizeIdentifier(String value) {
        return value == null ? "" : value.trim();
    }

    public String defaultNickname(String input, String fallback) {
        return input == null || input.isBlank() ? fallback : input;
    }

    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < PASSWORD_MIN_LEN || password.length() > PASSWORD_MAX_LEN) {
            throw new IllegalArgumentException("密码长度需在 " + PASSWORD_MIN_LEN + "~" + PASSWORD_MAX_LEN + " 位之间");
        }
    }

    public boolean verifyPasswordAndMigrateIfLegacy(String rawPassword, User user, Consumer<User> migrateAction) {
        String stored = user.getPassword();
        if (stored == null || stored.isBlank()) {
            return false;
        }

        boolean looksLikeBcrypt = stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
        if (looksLikeBcrypt) {
            return passwordEncoder.matches(rawPassword, stored);
        }

        if (stored.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            migrateAction.accept(user);
            return true;
        }

        try {
            return passwordEncoder.matches(rawPassword, stored);
        } catch (Exception ignore) {
            return false;
        }
    }

    public String encode(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(String rawValue, String encodedValue) {
        if (rawValue == null || encodedValue == null || encodedValue.isBlank()) {
            return false;
        }
        try {
            return passwordEncoder.matches(rawValue, encodedValue);
        } catch (Exception ignore) {
            return false;
        }
    }
}
