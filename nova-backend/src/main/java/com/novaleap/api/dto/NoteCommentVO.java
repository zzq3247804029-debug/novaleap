package com.novaleap.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteCommentVO {
    private Long id;
    private Long noteId;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private Boolean mine;
}

