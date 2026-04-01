package com.novaleap.api.module.admin.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserSaveRequest {

    @Size(max = 64, message = "用户名长度不能超过 64 个字符")
    private String username;

    @Size(max = 128, message = "密码长度不能超过 128 个字符")
    private String password;

    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    private String nickname;

    @Size(max = 16, message = "角色长度不能超过 16 个字符")
    private String role;
}
