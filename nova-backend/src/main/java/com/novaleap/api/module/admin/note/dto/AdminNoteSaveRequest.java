package com.novaleap.api.module.admin.note.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminNoteSaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题长度不能超过 120 个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 20000, message = "内容长度不能超过 20000 个字符")
    private String content;

    @Size(max = 280, message = "摘要长度不能超过 280 个字符")
    private String summary;

    @Size(max = 64, message = "分类长度不能超过 64 个字符")
    private String category;

    @Size(max = 16, message = "表情长度不能超过 16 个字符")
    private String emoji;

    @Size(max = 64, message = "作者长度不能超过 64 个字符")
    private String author;

    private Long userId;

    @Min(value = 0, message = "浏览次数不能为负数")
    private Integer viewCount;

    private Integer status;

    @Size(max = 240, message = "驳回原因长度不能超过 240 个字符")
    private String rejectReason;
}
