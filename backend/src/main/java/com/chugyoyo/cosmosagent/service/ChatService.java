package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.ChatRequest;
import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import com.chugyoyo.cosmosagent.dto.ChatSessionDTO;

import java.util.List;

public interface ChatService {
    
    ChatSessionDTO createOrGetSession(Long agentId, String sessionName);
    
    ChatMessageDTO sendMessage(ChatRequest request);
    
    ChatSessionDTO getDefaultSessionByAgentId(Long agentId);
    
    List<ChatSessionDTO> getAgentSessions(Long agentId);
}