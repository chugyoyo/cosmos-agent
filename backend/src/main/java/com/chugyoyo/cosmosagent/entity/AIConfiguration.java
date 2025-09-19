package com.chugyoyo.cosmosagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("ai_configurations")
@Data
public class AIConfiguration {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "provider")
    private String provider;
    
    @TableField(value = "api_key")
    private String apiKey;
    
    @TableField(value = "base_url")
    private String baseUrl;
    
    @TableField(value = "model")
    private String model;
    
    @TableField(value = "is_active")
    private Boolean isActive;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}