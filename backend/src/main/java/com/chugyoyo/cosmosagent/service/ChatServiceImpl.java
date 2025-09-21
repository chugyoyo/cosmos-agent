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

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ZhipuaiService zhipuaiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

            // 使用LangChain4j调用智谱AI服务获取流式回复
            StringBuilder aiResponseBuilder = new StringBuilder();
            return zhipuaiService.streamChat(request.getMessage())
                    .doOnNext(content -> {
                        // 累积AI回复内容
                        log.debug("收到AI回复片段: {}", content);
                        aiResponseBuilder.append(content);
                    })
                    .doOnComplete(() -> {
                        // 流式响应完成，保存完整的AI回复
                        String fullResponse = aiResponseBuilder.toString();
                        if (!fullResponse.isEmpty()) {
                            ChatMessageDTO aiMessage = new ChatMessageDTO();
                            aiMessage.setSessionId(session.getId());
                            aiMessage.setRole("assistant");
                            aiMessage.setContent(fullResponse);
                            chatMessageService.createMessage(aiMessage);
                            log.info("AI回复完成并保存，会话ID: {}, 内容长度: {}", session.getId(), fullResponse.length());
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("AI服务调用失败", e);
                        // 即使出错也保存错误信息
                        String errorMessage = "抱歉，AI服务暂时不可用，请稍后再试。";
                        ChatMessageDTO errorMessageDto = new ChatMessageDTO();
                        errorMessageDto.setSessionId(session.getId());
                        errorMessageDto.setRole("assistant");
                        errorMessageDto.setContent(errorMessage);
                        chatMessageService.createMessage(errorMessageDto);
                        return Flux.just(errorMessage);
                    });

        } catch (Exception e) {
            log.error("处理流式消息失败", e);
            return Flux.error(new RuntimeException("处理消息失败: " + e.getMessage()));
        }
    }
}