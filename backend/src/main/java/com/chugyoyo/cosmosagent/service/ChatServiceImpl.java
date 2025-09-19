package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.ChatRequest;
import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import com.chugyoyo.cosmosagent.dto.ChatSessionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ZhipuaiService zhipuaiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AgentOrchestrationService agentOrchestrationService;

    @Override
    @Transactional
    public ChatSessionDTO createOrGetSession(Long agentId, String sessionName) {
        List<ChatSessionDTO> existingSessions = chatSessionService.getLatestSessionsByAgentId(agentId, 1);
        
        if (!existingSessions.isEmpty()) {
            return existingSessions.get(0);
        }
        
        ChatSessionDTO sessionDTO = new ChatSessionDTO();
        sessionDTO.setAgentId(agentId);
        sessionDTO.setName(sessionName);
        sessionDTO.setStatus(0);
        
        return chatSessionService.createSession(sessionDTO);
    }
    
    @Override
    public ChatSessionDTO getDefaultSessionByAgentId(Long agentId) {
        List<ChatSessionDTO> sessions = chatSessionService.getLatestSessionsByAgentId(agentId, 1);
        return sessions.isEmpty() ? null : sessions.get(0);
    }
    
    @Override
    public List<ChatSessionDTO> getAgentSessions(Long agentId) {
        return chatSessionService.getSessionsByAgentId(agentId);
    }
    
    @Override
    @Transactional
    public Flux<String> sendMessageStream(ChatRequest request) {
        try {
            // 获取或创建会话
            ChatSessionDTO session;
            if (request.getSessionId() != null) {
                session = chatSessionService.getSessionById(request.getSessionId());
                if (session == null) {
                    return Flux.error(new RuntimeException("会话不存在"));
                }
            } else {
                session = createOrGetSession(request.getAgentId(), "默认会话 " + LocalDateTime.now());
            }
            
            // 保存用户消息
            ChatMessageDTO userMessage = new ChatMessageDTO();
            userMessage.setSessionId(session.getId());
            userMessage.setRole("user");
            userMessage.setContent(request.getMessage());
            chatMessageService.createMessage(userMessage);

            // 获取会话历史
            List<ChatMessageDTO> historyMessages = chatMessageService.getMessagesBySessionId(session.getId());
            String conversationHistory = buildConversationHistory(historyMessages);

            // 调用智谱AI服务获取流式回复
            return zhipuaiService.chatWithHistory(request.getMessage(), "glm-4", conversationHistory)
                    .doOnNext(content -> {
                        // 当收到完整内容时，保存AI回复
                        if (content != null && !content.isEmpty() && !content.equals("[DONE]")) {
                            log.debug("收到AI回复片段: {}", content);
                        }
                    })
                    .doOnComplete(() -> {
                        log.info("AI回复完成，会话ID: {}", session.getId());
                    })
                    .onErrorResume(e -> {
                        log.error("AI服务调用失败", e);
                        return Flux.just("抱歉，AI服务暂时不可用，请稍后再试。");
                    });
                    
        } catch (Exception e) {
            log.error("处理流式消息失败", e);
            return Flux.error(new RuntimeException("处理消息失败: " + e.getMessage()));
        }
    }
    
    private String buildConversationHistory(List<ChatMessageDTO> messages) {
        try {
            List<Map<String, String>> history = new ArrayList<>();
            
            // 只保留最近10条消息作为上下文
            int startIndex = Math.max(0, messages.size() - 10);
            for (int i = startIndex; i < messages.size(); i++) {
                ChatMessageDTO msg = messages.get(i);
                Map<String, String> messageMap = new HashMap<>();
                messageMap.put("role", msg.getRole());
                messageMap.put("content", msg.getContent());
                history.add(messageMap);
            }
            
            return objectMapper.writeValueAsString(history);
        } catch (Exception e) {
            log.warn("构建对话历史失败", e);
            return "[]";
        }
    }
}