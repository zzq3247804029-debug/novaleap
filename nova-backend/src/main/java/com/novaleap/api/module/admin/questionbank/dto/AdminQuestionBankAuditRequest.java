package com.novaleap.api.module.admin.questionbank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminQuestionBankAuditRequest {

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @Size(max = 120, message = "题库名称长度不能超过 120 个字符")
    private String name;

    @Size(max = 64, message = "分类长度不能超过 64 个字符")
    private String category;

    private Integer difficulty;

    @Size(max = 240, message = "驳回原因长度不能超过 240 个字符")
    private String rejectReason;
}
