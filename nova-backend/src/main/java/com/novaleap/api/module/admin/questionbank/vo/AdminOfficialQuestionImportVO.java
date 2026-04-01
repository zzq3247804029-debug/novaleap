package com.novaleap.api.module.admin.questionbank.vo;

import lombok.Data;

@Data
public class AdminOfficialQuestionImportVO {

    private String name;
    private String category;
    private Integer difficulty;
    private Integer parsedQuestionCount;
    private Integer importedQuestionCount;
}
