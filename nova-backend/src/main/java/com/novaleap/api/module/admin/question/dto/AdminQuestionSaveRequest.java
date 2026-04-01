package com.novaleap.api.module.admin.question.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminQuestionSaveRequest {

    @NotBlank(message = "题目标题不能为空")
    @Size(max = 255, message = "题目标题长度不能超过 255 个字符")
    private String title;

    private String content;

    @NotBlank(message = "标准答案不能为空")
    @Size(max = 20000, message = "标准答案长度不能超过 20000 个字符")
    private String standardAnswer;

    @Size(max = 64, message = "分类长度不能超过 64 个字符")
    private String category;

    @Min(value = 1, message = "难度不合法")
    @Max(value = 3, message = "难度不合法")
    private Integer difficulty;

    @Size(max = 255, message = "标签长度不能超过 255 个字符")
    private String tags;

    @Min(value = 0, message = "状态不合法")
    @Max(value = 1, message = "状态不合法")
    private Integer status;

    @Min(value = 0, message = "浏览次数不能为负数")
    private Integer viewCount;
}
