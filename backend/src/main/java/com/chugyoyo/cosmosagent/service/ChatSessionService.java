package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.ChatSessionDTO;
import java.util.List;

public interface ChatSessionService {
    
    List<ChatSessionDTO> getSessionsByAgentId(Long agentId);
    
    ChatSessionDTO getSessionById(Long id);
    
    ChatSessionDTO createSession(ChatSessionDTO dto);
    
    ChatSessionDTO updateSession(Long id, ChatSessionDTO dto);
    
    void deleteSession(Long id);
    
    List<ChatSessionDTO> getLatestSessionsByAgentId(Long agentId, Integer limit);
}