package com.novaleap.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WishRequest {
    @NotBlank(message = "愿望内容不能为空")
    private String content;
    private String city;
}
