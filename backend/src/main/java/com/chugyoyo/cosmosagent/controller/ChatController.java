package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.ApiResp;
import com.chugyoyo.cosmosagent.dto.ChatRequest;
import com.chugyoyo.cosmosagent.dto.ChatSessionDTO;
import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import com.chugyoyo.cosmosagent.service.ChatService;
import com.chugyoyo.cosmosagent.service.ChatSessionService;
import com.chugyoyo.cosmosagent.service.ChatMessageService;
import com.chugyoyo.cosmosagent.service.ZhipuaiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ChatController {
    
    private final ChatService chatService;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @GetMapping("/getSessionsByAgentId")
    public ApiResp<List<ChatSessionDTO>> getSessionsByAgent(@RequestParam Long agentId) {
        List<ChatSessionDTO> sessions = chatService.getAgentSessions(agentId);
        return ApiResp.success(sessions);
    }
    
    @PostMapping("/createSession")
    public ApiResp<ChatSessionDTO> createSession(@RequestBody ChatSessionDTO sessionDTO) {
        ChatSessionDTO session = chatSessionService.createSession(sessionDTO);
        return ApiResp.success(session);
    }
    
    @GetMapping("/getSessionMessages")
    public ApiResp<List<ChatMessageDTO>> getSessionMessages(@RequestParam Long sessionId) {
        List<ChatMessageDTO> messages = chatMessageService.getMessagesBySessionId(sessionId);
        return ApiResp.success(messages);
    }
    
    @PostMapping(value = "/sendMessageStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(@Valid @RequestBody ChatRequest request) {
        return chatService.sendMessageStreamV2(request);
    }

    @DeleteMapping("/deleteSession")
    public ApiResp<Void> deleteSession(@RequestParam Long sessionId) {
        chatSessionService.deleteSession(sessionId);
        return ApiResp.success();
    }
}