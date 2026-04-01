package com.novaleap.api.module.questionbank.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomQuestionBankVO {

    private Long id;
    private Long userId;
    private String name;
    private String originalFileName;
    private String fileType;
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
}
