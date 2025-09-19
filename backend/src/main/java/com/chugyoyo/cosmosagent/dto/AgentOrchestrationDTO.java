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
public class AgentOrchestrationDTO {
    
    private Long id;
    
    @NotBlank(message = "编排名称不能为空")
    private String name;
    
    private String description;
    
    private String flowData;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String config;
    
    private java.util.List<AgentOrchestrationNodeDTO> nodes;
}