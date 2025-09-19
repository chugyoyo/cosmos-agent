package com.chugyoyo.cosmosagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("agents")
@Data
public class Agent {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "name")
    private String name;
    
    @TableField(value = "type")
    private String type;
    
    @TableField(value = "description")
    private String description;
    
    @TableField(value = "configuration")
    private String configuration;
    
    @TableField(value = "status")
    private String status;
    
    @TableField(value = "call_count")
    private Long callCount;
    
    @TableField(value = "last_called")
    private String lastCalled;
    
    @TableField(value = "is_deployed")
    private Boolean isDeployed;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}