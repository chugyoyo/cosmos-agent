package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.dto.AgentDTO;
import com.chugyoyo.cosmosagent.entity.Agent;
import com.chugyoyo.cosmosagent.mapper.AgentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentService extends ServiceImpl<AgentMapper, Agent> {
    
    public List<AgentDTO> getAllAgents() {
        return list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AgentDTO> getAgentsByType(String type) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getType, type);
        return list(wrapper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AgentDTO> getDeployedAgents() {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Agent::getIsDeployed, true); TODO
        return list(wrapper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public AgentDTO getAgentById(Long id) {
        Agent entity = getById(id);
        return entity != null ? convertToDTO(entity) : null;
    }
    
    @Transactional
    public AgentDTO createAgent(AgentDTO dto) {
        Agent entity = convertToEntity(dto);
        boolean result = save(entity);
        return result ? convertToDTO(entity) : null;
    }
    
    @Transactional
    public AgentDTO updateAgent(Long id, AgentDTO dto) {
        Agent entity = getById(id);
        if (entity == null) {
            throw new RuntimeException("Agent not found");
        }
        
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        entity.setConfiguration(dto.getConfiguration());
        entity.setStatus(dto.getStatus());
        entity.setIsDeployed(dto.getIsDeployed());
        
        boolean result = updateById(entity);
        return result ? convertToDTO(entity) : null;
    }
    
    @Transactional
    public void deleteAgent(Long id) {
        removeById(id);
    }
    
    @Transactional
    public AgentDTO deployAgent(Long id) {
        LambdaUpdateWrapper<Agent> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Agent::getId, id)
                   .set(Agent::getIsDeployed, true)
                   .set(Agent::getStatus, "DEPLOYED");
        
        boolean result = update(updateWrapper);
        if (result) {
            return getAgentById(id);
        }
        return null;
    }
    
    @Transactional
    public AgentDTO undeployAgent(Long id) {
        LambdaUpdateWrapper<Agent> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Agent::getId, id)
                   .set(Agent::getIsDeployed, false)
                   .set(Agent::getStatus, "STOPPED");
        
        boolean result = update(updateWrapper);
        if (result) {
            return getAgentById(id);
        }
        return null;
    }
    
    @Transactional
    public AgentDTO incrementCallCount(Long id) {
        LambdaUpdateWrapper<Agent> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Agent::getId, id)
                   .setSql("call_count = call_count + 1")
                   .set(Agent::getLastCalled, LocalDateTime.now().toString());
        
        boolean result = update(updateWrapper);
        if (result) {
            return getAgentById(id);
        }
        return null;
    }
    
    private AgentDTO convertToDTO(Agent entity) {
        AgentDTO dto = new AgentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setConfiguration(entity.getConfiguration());
        dto.setStatus(entity.getStatus());
        dto.setCallCount(entity.getCallCount());
        dto.setLastCalled(entity.getLastCalled());
        dto.setIsDeployed(entity.getIsDeployed());
        return dto;
    }
    
    private Agent convertToEntity(AgentDTO dto) {
        Agent entity = new Agent();
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        entity.setConfiguration(dto.getConfiguration());
        entity.setStatus(dto.getStatus());
        entity.setCallCount(dto.getCallCount());
        entity.setLastCalled(dto.getLastCalled());
        entity.setIsDeployed(dto.getIsDeployed());
        return entity;
    }
}