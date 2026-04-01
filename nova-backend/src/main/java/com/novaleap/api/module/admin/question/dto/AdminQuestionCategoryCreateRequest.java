package com.novaleap.api.module.admin.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminQuestionCategoryCreateRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过 64 个字符")
    private String name;

    @Size(max = 64, message = "分类编码长度不能超过 64 个字符")
    private String code;
}
