package com.novaleap.api.module.admin.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private LocalDateTime createdAt;
}
