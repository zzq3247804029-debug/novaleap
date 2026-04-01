package com.novaleap.api.module.system.security;

public record CurrentUser(
        String username,
        String role,
        boolean anonymous,
        boolean guest,
        boolean admin
) {

    public boolean isAuthenticated() {
        return !anonymous;
    }

    public boolean isMember() {
        return isAuthenticated() && !guest;
    }

    public String safeUsername() {
        return username == null ? "" : username.trim();
    }
}
