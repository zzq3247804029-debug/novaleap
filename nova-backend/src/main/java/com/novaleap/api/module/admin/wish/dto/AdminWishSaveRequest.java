package com.novaleap.api.module.admin.wish.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminWishSaveRequest {

    @Size(max = 500, message = "愿望内容长度不能超过 500 个字符")
    private String content;

    @Size(max = 32, message = "情绪长度不能超过 32 个字符")
    private String emotion;

    @Size(max = 32, message = "颜色长度不能超过 32 个字符")
    private String color;

    @Size(max = 64, message = "城市长度不能超过 64 个字符")
    private String city;

    private Integer posX;
    private Integer posY;
    private Double floatSpeed;
    private Integer status;
}
