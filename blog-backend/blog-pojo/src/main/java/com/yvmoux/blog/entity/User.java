package com.yvmoux.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String avatar;

    private String role;

    private String status;

    @TableField(exist = false)
    private Integer articleCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
