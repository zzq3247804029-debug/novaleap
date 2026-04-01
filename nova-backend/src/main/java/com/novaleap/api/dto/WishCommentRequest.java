package com.novaleap.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WishCommentRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 300, message = "评论内容不能超过 300 个字符")
    private String content;
}
