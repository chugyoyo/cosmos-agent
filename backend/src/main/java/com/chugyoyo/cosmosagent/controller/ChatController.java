package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.ApiResp;
import com.chugyoyo.cosmosagent.dto.ChatRequest;
import com.chugyoyo.cosmosagent.dto.ChatSessionDTO;
import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import com.chugyoyo.cosmosagent.dto.AgentDTO;
import com.chugyoyo.cosmosagent.service.ChatService;
import com.chugyoyo.cosmosagent.service.ChatSessionService;
import com.chugyoyo.cosmosagent.service.ChatMessageService;
import com.chugyoyo.cosmosagent.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final ChatService chatService;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final AgentService agentService;
    
    @GetMapping("/agents")
    public ApiResp<List<AgentDTO>> getAvailableAgents() {
        // 只返回已部署的Agent
        List<AgentDTO> deployedAgents = agentService.getDeployedAgents();
        return ApiResp.success(deployedAgents);
    }
    
    @GetMapping("/sessions/{agentId}")
    public ApiResp<List<ChatSessionDTO>> getSessionsByAgent(@PathVariable Long agentId) {
        List<ChatSessionDTO> sessions = chatService.getAgentSessions(agentId);
        return ApiResp.success(sessions);
    }
    
    @PostMapping("/sessions")
    public ApiResp<ChatSessionDTO> createSession(@RequestBody ChatSessionDTO sessionDTO) {
        ChatSessionDTO session = chatSessionService.createSession(sessionDTO);
        return ApiResp.success(session);
    }
    
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResp<List<ChatMessageDTO>> getSessionMessages(@PathVariable Long sessionId) {
        List<ChatMessageDTO> messages = chatMessageService.getMessagesBySessionId(sessionId);
        return ApiResp.success(messages);
    }
    
    @PostMapping("/send")
    public ApiResp<ChatMessageDTO> sendMessage(@Valid @RequestBody ChatRequest request) {
        ChatMessageDTO response = chatService.sendMessage(request);
        return ApiResp.success(response);
    }
    
    @GetMapping("/default-session/{agentId}")
    public ApiResp<ChatSessionDTO> getDefaultSession(@PathVariable Long agentId) {
        ChatSessionDTO session = chatService.getDefaultSessionByAgentId(agentId);
        if (session == null) {
            // 如果没有默认会话，创建一个
            ChatSessionDTO newSession = new ChatSessionDTO();
            newSession.setAgentId(agentId);
            newSession.setName("默认会话");
            session = chatSessionService.createSession(newSession);
        }
        return ApiResp.success(session);
    }
    
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResp<Void> deleteSession(@PathVariable Long sessionId) {
        chatSessionService.deleteSession(sessionId);
        return ApiResp.success();
    }
}