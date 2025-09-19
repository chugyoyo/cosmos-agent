package com.chugyoyo.cosmosagent.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentOrchestrationNodeDTO {
    
    private Long id;
    
    @NotNull(message = "编排ID不能为空")
    private Long orchestrationId;
    
    @NotBlank(message = "节点名称不能为空")
    private String name;
    
    @NotBlank(message = "节点类型不能为空")
    private String type;
    
    private Integer status;
    
    @NotNull(message = "X坐标不能为空")
    private Integer positionX;
    
    @NotNull(message = "Y坐标不能为空")
    private Integer positionY;
    
    private String config;
    
    private String yamlConfig;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}