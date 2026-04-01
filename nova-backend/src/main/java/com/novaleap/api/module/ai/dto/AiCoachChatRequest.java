package com.novaleap.api.module.ai.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiCoachChatRequest {

    @Size(max = 4000, message = "消息长度不能超过 4000 个字符")
    private String message;

    @Size(max = 120, message = "话题长度不能超过 120 个字符")
    private String topic;

    @Size(max = 500000, message = "图片数据过大")
    private String image;

    @Size(max = 40, message = "模式长度不能超过 40 个字符")
    private String mode;
}
