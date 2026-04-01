package com.novaleap.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("custom_question_banks")
public class CustomQuestionBank {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String originalFileName;
    private String fileType;
    private String rawContent;
    private String category;
    private Integer difficulty;
    private Integer status;
    private Integer questionCount;
    private Integer importedQuestionCount;
    private String rejectReason;
    private LocalDateTime auditedAt;
    private LocalDateTime importedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String ownerUsername;

    @TableField(exist = false)
    private String ownerNickname;

    @TableField(exist = false)
    private String previewText;
}
