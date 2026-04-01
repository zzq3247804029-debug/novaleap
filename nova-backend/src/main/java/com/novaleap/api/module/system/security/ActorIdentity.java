package com.novaleap.api.module.system.security;

public record ActorIdentity(String type, String id) {

    public boolean isEmpty() {
        return type == null || type.isBlank() || id == null || id.isBlank();
    }
}
