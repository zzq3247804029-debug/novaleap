package com.novaleap.api.module.admin.note.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminNoteStatusRequest {

    private Integer status;

    @Size(max = 240, message = "驳回原因长度不能超过 240 个字符")
    private String rejectReason;

    @Size(max = 240, message = "原因长度不能超过 240 个字符")
    private String reason;
}
