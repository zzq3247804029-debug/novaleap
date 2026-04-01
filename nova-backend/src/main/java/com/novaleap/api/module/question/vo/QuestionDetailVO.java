package com.novaleap.api.module.question.vo;

import lombok.Data;

@Data
public class QuestionDetailVO {
    private Long id;
    private String title;
    private String content;
    private Integer difficulty;
    private String category;
    private String tags;
    private Integer viewCount;
    private String sourceType;
}
