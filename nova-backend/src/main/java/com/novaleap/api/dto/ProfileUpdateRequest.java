package com.novaleap.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    private String nickname;

    @Size(max = 128, message = "密码长度不能超过 128 个字符")
    private String password;

    @Size(max = 8, message = "头像参数不合法")
    private String avatar;
}
