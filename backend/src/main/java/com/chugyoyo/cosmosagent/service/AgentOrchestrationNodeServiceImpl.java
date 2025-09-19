package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.entity.AgentOrchestrationNode;
import com.chugyoyo.cosmosagent.mapper.AgentOrchestrationNodeMapper;
import com.chugyoyo.cosmosagent.dto.AgentOrchestrationNodeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentOrchestrationNodeServiceImpl extends ServiceImpl<AgentOrchestrationNodeMapper, AgentOrchestrationNode> implements AgentOrchestrationNodeService {
    
    private final AgentOrchestrationNodeMapper nodeMapper;
    
    @Override
    public List<AgentOrchestrationNodeDTO> getNodesByOrchestrationId(Long orchestrationId) {
        LambdaQueryWrapper<AgentOrchestrationNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentOrchestrationNode::getOrchestrationId, orchestrationId);
        wrapper.orderByAsc(AgentOrchestrationNode::getId);
        
        List<AgentOrchestrationNode> nodes = nodeMapper.selectList(wrapper);
        return nodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AgentOrchestrationNodeDTO> getAllNodes() {
        List<AgentOrchestrationNode> nodes = nodeMapper.selectList(null);
        return nodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AgentOrchestrationNodeDTO getNodeById(Long id) {
        AgentOrchestrationNode node = nodeMapper.selectById(id);
        return node != null ? convertToDTO(node) : null;
    }
    
    @Override
    @Transactional
    public AgentOrchestrationNodeDTO createNode(AgentOrchestrationNodeDTO dto) {
        AgentOrchestrationNode node = convertToEntity(dto);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        nodeMapper.insert(node);
        return convertToDTO(node);
    }
    
    @Override
    @Transactional
    public AgentOrchestrationNodeDTO updateNode(Long id, AgentOrchestrationNodeDTO dto) {
        AgentOrchestrationNode existing = nodeMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("节点不存在");
        }
        
        AgentOrchestrationNode node = convertToEntity(dto);
        node.setId(id);
        node.setOrchestrationId(existing.getOrchestrationId());
        node.setCreatedAt(existing.getCreatedAt());
        node.setUpdatedAt(LocalDateTime.now());
        nodeMapper.updateById(node);
        return convertToDTO(node);
    }
    
    @Override
    @Transactional
    public void deleteNode(Long id) {
        nodeMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public AgentOrchestrationNodeDTO updateNodeYaml(Long id, String yamlConfig) {
        AgentOrchestrationNode node = nodeMapper.selectById(id);
        if (node == null) {
            throw new RuntimeException("节点不存在");
        }
        
        node.setYamlConfig(yamlConfig);
        node.setUpdatedAt(LocalDateTime.now());
        nodeMapper.updateById(node);
        return convertToDTO(node);
    }
    
    private AgentOrchestrationNodeDTO convertToDTO(AgentOrchestrationNode entity) {
        AgentOrchestrationNodeDTO dto = new AgentOrchestrationNodeDTO();
        dto.setId(entity.getId());
        dto.setOrchestrationId(entity.getOrchestrationId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        dto.setPositionX(entity.getPositionX());
        dto.setPositionY(entity.getPositionY());
        dto.setConfig(entity.getConfig());
        dto.setYamlConfig(entity.getYamlConfig());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    private AgentOrchestrationNode convertToEntity(AgentOrchestrationNodeDTO dto) {
        AgentOrchestrationNode entity = new AgentOrchestrationNode();
        entity.setOrchestrationId(dto.getOrchestrationId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setStatus(dto.getStatus());
        entity.setPositionX(dto.getPositionX());
        entity.setPositionY(dto.getPositionY());
        entity.setConfig(dto.getConfig());
        entity.setYamlConfig(dto.getYamlConfig());
        return entity;
    }
}