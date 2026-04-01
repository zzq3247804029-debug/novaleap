package com.novaleap.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteLikeVO {
    private Long noteId;
    private Boolean liked;
    private Long likeCount;
}

