package com.novaleap.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishWallItemVO {
    private Long id;
    private String content;
    private String emotion;
    private String color;
    private String city;
    private Integer posX;
    private Integer posY;
    private Double floatSpeed;
    private Integer status;
    private LocalDateTime createdAt;

    private Long likeCount;
    private Long commentCount;
    private Boolean likedByMe;
}
