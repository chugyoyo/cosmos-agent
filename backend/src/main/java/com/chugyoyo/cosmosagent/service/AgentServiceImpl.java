package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.entity.Agent;
import com.chugyoyo.cosmosagent.mapper.AgentMapper;
import com.chugyoyo.cosmosagent.dto.AgentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {
    
    private final AgentMapper agentMapper;
    
    @Override
    public List<AgentDTO> getAllAgents() {
        List<Agent> agents = agentMapper.selectList(null);
        return agents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AgentDTO getAgentById(Long id) {
        Agent agent = agentMapper.selectById(id);
        return agent != null ? convertToDTO(agent) : null;
    }

    @Override
    @Transactional
    public AgentDTO createAgent(AgentDTO dto) {
        Agent agent = convertToEntity(dto);
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        agent.setStatus(0);
        agentMapper.insert(agent);
        return convertToDTO(agent);
    }
    
    @Override
    @Transactional
    public AgentDTO updateAgent(Long id, AgentDTO dto) {
        Agent existing = agentMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("代理不存在");
        }
        
        Agent agent = convertToEntity(dto);
        agent.setId(id);
        agent.setCreatedAt(existing.getCreatedAt());
        agent.setUpdatedAt(LocalDateTime.now());
        agentMapper.updateById(agent);
        return convertToDTO(agent);
    }
    
    @Override
    @Transactional
    public void deleteAgent(Long id) {
        agentMapper.deleteById(id);
    }
    
    private AgentDTO convertToDTO(Agent entity) {
        AgentDTO dto = new AgentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setConfig(entity.getConfig());
        return dto;
    }
    
    private Agent convertToEntity(AgentDTO dto) {
        Agent entity = new Agent();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setConfig(dto.getConfig());
        return entity;
    }
}