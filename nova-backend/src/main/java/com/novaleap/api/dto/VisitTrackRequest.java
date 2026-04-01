package com.novaleap.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VisitTrackRequest {

    @NotBlank(message = "访客标识不能为空")
    @Size(max = 64, message = "访客标识长度不能超过 64 个字符")
    private String visitorId;

    @Size(max = 300, message = "访问路径长度不能超过 300 个字符")
    private String path;
}
