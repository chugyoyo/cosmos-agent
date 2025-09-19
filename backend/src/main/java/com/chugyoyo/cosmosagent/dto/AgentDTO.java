package com.chugyoyo.cosmosagent.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AgentDTO {
    
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Type is required")
    private String type;
    
    private String description;
    private String configuration;
    private String status;
    private Long callCount;
    private String lastCalled;
    private Boolean isDeployed;
    private String createdAt;
    private String updatedAt;
}