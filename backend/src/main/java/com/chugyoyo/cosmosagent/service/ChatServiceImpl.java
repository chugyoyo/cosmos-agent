package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.ChatRequest;
import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import com.chugyoyo.cosmosagent.dto.ChatSessionDTO;
import com.chugyoyo.cosmosagent.dto.AgentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final AgentService agentService;
    
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
    @Transactional
    public ChatMessageDTO sendMessage(ChatRequest request) {
        // 获取或创建会话
        ChatSessionDTO session;
        if (request.getSessionId() != null) {
            session = chatSessionService.getSessionById(request.getSessionId());
            if (session == null) {
                throw new RuntimeException("会话不存在");
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
        
        // 获取Agent信息
        AgentDTO agent = agentService.getAgentById(request.getAgentId());
        if (agent == null) {
            throw new RuntimeException("Agent不存在");
        }
        
        // 生成AI回复（这里暂时使用简单的回复逻辑）
        String aiResponse = generateAIResponse(request.getMessage(), agent);
        
        // 保存AI回复
        ChatMessageDTO aiMessage = new ChatMessageDTO();
        aiMessage.setSessionId(session.getId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        return chatMessageService.createMessage(aiMessage);
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
    
    private String generateAIResponse(String userMessage, AgentDTO agent) {
        // 这里是一个简单的AI回复逻辑，实际应用中应该调用真正的AI服务
        String response;
        
        if (userMessage.contains("你好") || userMessage.contains("hello")) {
            response = "你好！我是" + agent.getName() + "，很高兴为您服务！我可以帮助您回答问题、处理任务等。请问有什么可以帮助您的吗？";
        } else if (userMessage.contains("帮助") || userMessage.contains("help")) {
            response = "我可以为您提供以下帮助：\n" +
                       "1. 回答您的问题\n" +
                       "2. 处理文本任务\n" +
                       "3. 数据分析\n" +
                       "4. 生成报告\n" +
                       "请告诉我您具体需要什么帮助？";
        } else if (userMessage.contains("再见") || userMessage.contains("bye")) {
            response = "再见！感谢您的使用，如果还有其他问题，随时可以找我。祝您生活愉快！";
        } else {
            response = "感谢您的问题：" + userMessage + "\n\n" +
                       "作为" + agent.getName() + "，我正在处理您的请求。这是一个示例回复。\n\n" +
                       "在实际应用中，这里会连接到真正的AI服务（如OpenAI、智谱AI等）来生成智能回复。\n\n" +
                       "请问还有其他可以帮助您的吗？";
        }
        
        return response;
    }
}