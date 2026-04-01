package com.novaleap.api.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiNoteSummaryRequest {

    @Size(max = 200, message = "标题长度不能超过 200 个字符")
    private String title;

    @NotBlank(message = "笔记内容不能为空")
    @Size(max = 20000, message = "笔记内容长度不能超过 20000 个字符")
    private String content;
}
