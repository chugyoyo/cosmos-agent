package com.chugyoyo.cosmosagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_link")
public class AgentLink {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long agentId;
    
    private Long sourceNodeId;
    
    private Long targetNodeId;
    
    private String linkType;
    
    private String name;
    
    private String description;
    
    private String condition;
    
    private Integer sortOrder;
    
    private Integer status;
    
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String config;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}