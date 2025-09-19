package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.entity.ChatSession;
import com.chugyoyo.cosmosagent.mapper.ChatSessionMapper;
import com.chugyoyo.cosmosagent.dto.ChatSessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {
    
    private final ChatSessionMapper sessionMapper;
    
    @Override
    public List<ChatSessionDTO> getSessionsByAgentId(Long agentId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getAgentId, agentId);
        wrapper.orderByDesc(ChatSession::getUpdatedAt);
        
        List<ChatSession> sessions = sessionMapper.selectList(wrapper);
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ChatSessionDTO getSessionById(Long id) {
        ChatSession session = sessionMapper.selectById(id);
        return session != null ? convertToDTO(session) : null;
    }
    
    @Override
    @Transactional
    public ChatSessionDTO createSession(ChatSessionDTO dto) {
        ChatSession session = convertToEntity(dto);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setStatus(0);
        sessionMapper.insert(session);
        return convertToDTO(session);
    }
    
    @Override
    @Transactional
    public ChatSessionDTO updateSession(Long id, ChatSessionDTO dto) {
        ChatSession existing = sessionMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("会话不存在");
        }
        
        ChatSession session = convertToEntity(dto);
        session.setId(id);
        session.setCreatedAt(existing.getCreatedAt());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
        return convertToDTO(session);
    }
    
    @Override
    @Transactional
    public void deleteSession(Long id) {
        sessionMapper.deleteById(id);
    }
    
    @Override
    public List<ChatSessionDTO> getLatestSessionsByAgentId(Long agentId, Integer limit) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getAgentId, agentId);
        wrapper.orderByDesc(ChatSession::getUpdatedAt);
        wrapper.last("LIMIT " + limit);
        
        List<ChatSession> sessions = sessionMapper.selectList(wrapper);
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private ChatSessionDTO convertToDTO(ChatSession entity) {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setId(entity.getId());
        dto.setAgentId(entity.getAgentId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    private ChatSession convertToEntity(ChatSessionDTO dto) {
        ChatSession entity = new ChatSession();
        entity.setAgentId(dto.getAgentId());
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus());
        return entity;
    }
}