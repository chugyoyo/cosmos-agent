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
public class ChatMessageDTO {
    
    private Long id;
    
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
    
    @NotBlank(message = "角色不能为空")
    private String role;
    
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    private LocalDateTime createdAt;
}