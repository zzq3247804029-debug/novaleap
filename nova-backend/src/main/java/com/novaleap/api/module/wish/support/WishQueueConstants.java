package com.novaleap.api.module.wish.support;

public final class WishQueueConstants {

    public static final String PENDING_QUEUE = "wish:pending";
    public static final String PROCESSING_QUEUE = "wish:processing";
    public static final String PROCESSING_LOCK_PREFIX = "wish:processing:lock:";
    public static final String RETRY_PREFIX = "wish:retry:";
    public static final String DEAD_LETTER_SET = "wish:dead";

    private WishQueueConstants() {
    }
}
