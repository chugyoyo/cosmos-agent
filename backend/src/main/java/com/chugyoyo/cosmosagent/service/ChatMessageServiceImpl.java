package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.entity.ChatMessage;
import com.chugyoyo.cosmosagent.mapper.ChatMessageMapper;
import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    
    private final ChatMessageMapper messageMapper;
    
    @Override
    public List<ChatMessageDTO> getMessagesBySessionId(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        wrapper.orderByAsc(ChatMessage::getCreatedAt);
        
        List<ChatMessage> messages = messageMapper.selectList(wrapper);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ChatMessageDTO createMessage(ChatMessageDTO dto) {
        ChatMessage message = convertToEntity(dto);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        return convertToDTO(message);
    }
    
    @Override
    public List<ChatMessageDTO> getLatestMessagesBySessionId(Long sessionId, Integer limit) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        wrapper.orderByDesc(ChatMessage::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        
        List<ChatMessage> messages = messageMapper.selectList(wrapper);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteMessagesBySessionId(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        messageMapper.delete(wrapper);
    }
    
    private ChatMessageDTO convertToDTO(ChatMessage entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(entity.getId());
        dto.setSessionId(entity.getSessionId());
        dto.setRole(entity.getRole());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
    
    private ChatMessage convertToEntity(ChatMessageDTO dto) {
        ChatMessage entity = new ChatMessage();
        entity.setSessionId(dto.getSessionId());
        entity.setRole(dto.getRole());
        entity.setContent(dto.getContent());
        return entity;
    }
}