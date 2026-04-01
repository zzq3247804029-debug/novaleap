package com.novaleap.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaderboardQuestionDoneRequest {

    @NotNull(message = "题目 ID 不能为空")
    private Long questionId;
}
