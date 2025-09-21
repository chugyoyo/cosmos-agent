package com.chugyoyo.cosmosagent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDTO {
    
    private Long id;
    
    @NotBlank(message = "代理名称不能为空")
    private String name;
    
    private String description;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    @JsonFormat
    private String config;
    
    private List<AgentNodeDTO> nodes;
}