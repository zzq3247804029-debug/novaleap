package com.novaleap.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库实体类
 */
@Data
@TableName("questions")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;       // 题目标题
    private String content;     // 题目详细描述
    private String standardAnswer; // 数据库标准答案（用于 AI 回答）
    private Integer difficulty; // 难度: 1简单 2中等 3困难
    private String category;    // 分类: java, spring, db, redis, algo, network
    private String tags;        // 标签 (逗号分隔)
    private Integer viewCount;  // 浏览次数
    private Integer status;     // 0: 下架 1: 上架
    private String sourceType;  // OFFICIAL / CUSTOM
    private Long customBankId;  // 自定义题库 ID
    private Long ownerUserId;   // 题库所属用户 ID
    private LocalDateTime createdAt;
}
