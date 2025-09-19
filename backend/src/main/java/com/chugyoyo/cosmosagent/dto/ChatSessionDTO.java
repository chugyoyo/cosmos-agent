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
public class ChatSessionDTO {
    
    private Long id;
    
    @NotNull(message = "Agent ID不能为空")
    private Long agentId;
    
    @NotBlank(message = "会话名称不能为空")
    private String name;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}