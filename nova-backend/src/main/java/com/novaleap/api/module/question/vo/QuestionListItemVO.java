package com.novaleap.api.module.question.vo;

import lombok.Data;

@Data
public class QuestionListItemVO {
    private Long id;
    private String title;
    private Integer difficulty;
    private String category;
    private String tags;
    private Integer viewCount;
    private String sourceType;
}
