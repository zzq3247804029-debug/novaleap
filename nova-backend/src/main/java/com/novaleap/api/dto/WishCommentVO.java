package com.novaleap.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishCommentVO {
    private Long id;
    private Long wishId;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private Boolean mine;
}
