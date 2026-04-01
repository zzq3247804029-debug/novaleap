package com.novaleap.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入正确的邮箱格式")
    @Size(max = 64, message = "邮箱长度不能超过 64 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 128, message = "密码长度不能超过 128 个字符")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Size(max = 128, message = "确认密码长度不能超过 128 个字符")
    private String confirmPassword;

    @NotBlank(message = "验证码不能为空")
    @Size(max = 10, message = "验证码长度不合法")
    private String emailCode;

    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    private String nickname;

    private Boolean consent;

    private String turnstileToken;
}
