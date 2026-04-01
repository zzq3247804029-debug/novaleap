package com.novaleap.api.dto;

import lombok.Data;

@Data
public class WishLikeVO {
    private Long wishId;
    private Boolean liked;
    private Long likeCount;
}
