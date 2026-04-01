package com.novaleap.api.module.admin.question.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminQuestionVO {
    private Long id;
    private String title;
    private String content;
    private String standardAnswer;
    private String category;
    private String categoryName;
    private Integer difficulty;
    private String tags;
    private Integer viewCount;
    private Integer status;
    private String sourceType;
    private Long customBankId;
    private Long ownerUserId;
    private LocalDateTime createdAt;
}
