package com.novaleap.api.module.admin.note.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminNoteListItemVO {
    private Long id;
    private String title;
    private String summary;
    private String category;
    private String emoji;
    private String author;
    private Long userId;
    private Integer viewCount;
    private Integer status;
    private String rejectReason;
    private String auditSource;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
