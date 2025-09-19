package com.chugyoyo.cosmosagent.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AIConfigurationDTO {
    
    private Long id;
    
    @NotBlank(message = "Provider is required")
    private String provider;
    
    @NotBlank(message = "API Key is required")
    private String apiKey;
    
    private String baseUrl;
    private String model;
    private Boolean isActive;
}