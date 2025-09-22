package com.chugyoyo.cosmosagent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentNodeDTO {
    
    private Long id;
    
    @NotNull(message = "代理ID不能为空")
    private Long agentId;
    
    @NotBlank(message = "代理节点名称不能为空")
    private String name;
    
    @NotBlank(message = "代理节点类型不能为空")
    private String type;
    
    private Integer status;
    
    @NotNull(message = "代理节点X坐标不能为空")
    private Integer positionX;
    
    @NotNull(message = "代理节点Y坐标不能为空")
    private Integer positionY;

    // TODO "JSON parse error: Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)"

    private String config;
    
    private String yamlConfig;
    
    private String llmConfig;
    
    private String startConfig;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}