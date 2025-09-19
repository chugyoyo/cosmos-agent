package com.chugyoyo.cosmosagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_message")
public class ChatMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sessionId;
    
    private String role;
    
    private String content;
    
    private LocalDateTime createdAt;
}