package com.novaleap.api.module.question.vo;

import lombok.Data;

@Data
public class QuestionAnswerVO {
    private Long id;
    private String title;
    private String answer;
    private String source;
}
