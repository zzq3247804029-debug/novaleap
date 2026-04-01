package com.novaleap.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wishes")
public class Wish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
    private String emotion; // 情绪分析结果 (happy, hopeful, confused, anxious)
    private String color; // UI 面板颜色
    private String city; 
    private Integer posX;
    private Integer posY;
    private Double floatSpeed;
    private Integer status; // 0: 待审核 1: 已上线 2: 违规
    private LocalDateTime createdAt;
}
