package com.yvmoux.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField(exist = false)
    private Integer articleCount;
}
