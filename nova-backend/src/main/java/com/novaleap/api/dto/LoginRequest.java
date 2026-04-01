package com.novaleap.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "账号不能为空")
    @Size(max = 64, message = "账号长度不能超过 64 个字符")
    private String username;

    @Size(max = 128, message = "密码长度不能超过 128 个字符")
    private String password;

    @Size(max = 10, message = "验证码长度不合法")
    private String emailCode;

    /**
     * password | code
     */
    private String loginType;

    private String turnstileToken;

    public boolean isCodeLogin() {
        return "code".equalsIgnoreCase(loginType);
    }
}
