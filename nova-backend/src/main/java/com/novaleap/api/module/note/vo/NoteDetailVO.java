package com.novaleap.api.module.note.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteDetailVO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String category;
    private String emoji;
    private String author;
    private Integer viewCount;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Long commentCount;
    private Boolean likedByMe;
    private Integer wordCount;
}
