package com.novaleap.api.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiResumeAnalyzeRequest {

    @NotBlank(message = "简历内容不能为空")
    @Size(max = 15000, message = "简历内容长度不能超过 15000 个字符")
    private String resumeText;

    @Size(max = 120, message = "目标岗位长度不能超过 120 个字符")
    private String targetRole;
}
