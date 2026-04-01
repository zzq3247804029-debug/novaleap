package com.novaleap.api.module.ai.vo;

import lombok.Data;

@Data
public class AiCoachHistoryItemVO {

    private String role;
    private String content;
    private String mode;
    private String topic;
    private String sessionId;
    private String timestamp;
}
