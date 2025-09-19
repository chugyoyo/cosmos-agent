package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.entity.AgentOrchestration;
import com.chugyoyo.cosmosagent.mapper.AgentOrchestrationMapper;
import com.chugyoyo.cosmosagent.dto.AgentOrchestrationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentOrchestrationServiceImpl extends ServiceImpl<AgentOrchestrationMapper, AgentOrchestration> implements AgentOrchestrationService {
    
    private final AgentOrchestrationMapper orchestrationMapper;
    
    @Override
    public List<AgentOrchestrationDTO> getAllOrchestrations() {
        List<AgentOrchestration> orchestrations = orchestrationMapper.selectList(null);
        return orchestrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AgentOrchestrationDTO getOrchestrationById(Long id) {
        AgentOrchestration orchestration = orchestrationMapper.selectById(id);
        return orchestration != null ? convertToDTO(orchestration) : null;
    }
    
    @Override
    @Transactional
    public AgentOrchestrationDTO createOrchestration(AgentOrchestrationDTO dto) {
        AgentOrchestration orchestration = convertToEntity(dto);
        orchestration.setCreatedAt(LocalDateTime.now());
        orchestration.setUpdatedAt(LocalDateTime.now());
        orchestration.setStatus(0);
        orchestrationMapper.insert(orchestration);
        return convertToDTO(orchestration);
    }
    
    @Override
    @Transactional
    public AgentOrchestrationDTO updateOrchestration(Long id, AgentOrchestrationDTO dto) {
        AgentOrchestration existing = orchestrationMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("编排不存在");
        }
        
        AgentOrchestration orchestration = convertToEntity(dto);
        orchestration.setId(id);
        orchestration.setCreatedAt(existing.getCreatedAt());
        orchestration.setUpdatedAt(LocalDateTime.now());
        orchestrationMapper.updateById(orchestration);
        return convertToDTO(orchestration);
    }
    
    @Override
    @Transactional
    public void deleteOrchestration(Long id) {
        orchestrationMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public AgentOrchestrationDTO updateFlowData(Long id, String flowData) {
        AgentOrchestration orchestration = orchestrationMapper.selectById(id);
        if (orchestration == null) {
            throw new RuntimeException("编排不存在");
        }
        
        orchestration.setFlowData(flowData);
        orchestration.setUpdatedAt(LocalDateTime.now());
        orchestrationMapper.updateById(orchestration);
        return convertToDTO(orchestration);
    }
    
    private AgentOrchestrationDTO convertToDTO(AgentOrchestration entity) {
        AgentOrchestrationDTO dto = new AgentOrchestrationDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setFlowData(entity.getFlowData());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setConfig(entity.getConfig());
        return dto;
    }
    
    private AgentOrchestration convertToEntity(AgentOrchestrationDTO dto) {
        AgentOrchestration entity = new AgentOrchestration();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setFlowData(dto.getFlowData());
        entity.setStatus(dto.getStatus());
        entity.setConfig(dto.getConfig());
        return entity;
    }
}