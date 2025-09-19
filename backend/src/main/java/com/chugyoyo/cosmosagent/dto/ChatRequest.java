package com.chugyoyo.cosmosagent.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotNull(message = "Agent ID不能为空")
    private Long agentId;
    
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    private Long sessionId;
}