package com.novaleap.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wish_likes")
public class WishLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long wishId;
    private String actorType;
    private String actorId;
    private LocalDateTime createdAt;
}
