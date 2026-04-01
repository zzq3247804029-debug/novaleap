package com.novaleap.api.module.admin.questionbank.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminQuestionBankVO {

    private Long id;
    private Long userId;
    private String name;
    private String originalFileName;
    private String fileType;
    private String rawContent;
    private String category;
    private Integer difficulty;
    private Integer status;
    private Integer questionCount;
    private Integer importedQuestionCount;
    private String rejectReason;
    private LocalDateTime auditedAt;
    private LocalDateTime importedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerUsername;
    private String ownerNickname;
    private String previewText;
}
