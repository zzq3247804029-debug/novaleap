package com.novaleap.api.module.admin.wish.vo;

import lombok.Data;

@Data
public class AdminWishQueueStatsVO {
    private long pendingQueueSize;
    private long processingQueueSize;
    private long deadLetterSize;
    private long pendingDbCount;
}
