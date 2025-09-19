package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import java.util.List;

public interface ChatMessageService {
    
    List<ChatMessageDTO> getMessagesBySessionId(Long sessionId);
    
    ChatMessageDTO createMessage(ChatMessageDTO dto);
    
    List<ChatMessageDTO> getLatestMessagesBySessionId(Long sessionId, Integer limit);
    
    void deleteMessagesBySessionId(Long sessionId);
}