package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.ChatRequest;
import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import com.chugyoyo.cosmosagent.dto.ChatSessionDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    
    ChatSessionDTO createOrGetSession(Long agentId, String sessionName);

    Flux<String> sendMessageStream(ChatRequest request);

    /**
     * 使用 tools、上下文
     * @param request
     * @return
     */
    Flux<String> sendMessageStreamV2(ChatRequest request);

    ChatSessionDTO getDefaultSessionByAgentId(Long agentId);
    
    List<ChatSessionDTO> getAgentSessions(Long agentId);
}