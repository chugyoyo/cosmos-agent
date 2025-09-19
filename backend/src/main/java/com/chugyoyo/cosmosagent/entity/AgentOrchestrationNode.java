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
@TableName("agent_orchestration_node")
public class AgentOrchestrationNode {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orchestrationId;
    
    private String name;
    
    private String type;
    
    private Integer status;
    
    private Integer positionX;
    
    private Integer positionY;
    
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String config;
    
    private String yamlConfig;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}