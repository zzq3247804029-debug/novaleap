package com.novaleap.api.module.questionbank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionBankRenameRequest {

    @NotBlank(message = "题库名称不能为空")
    @Size(max = 120, message = "题库名称长度不能超过 120 个字符")
    private String name;
}
