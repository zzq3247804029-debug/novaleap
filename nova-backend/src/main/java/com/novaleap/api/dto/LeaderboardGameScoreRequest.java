package com.novaleap.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LeaderboardGameScoreRequest {

    @NotNull(message = "分数不能为空")
    @Min(value = 0, message = "分数不能小于 0")
    private Integer score;

    @Size(max = 64, message = "回合标识长度不能超过 64 个字符")
    private String roundId;

    private Boolean finalScore;
}
