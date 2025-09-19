package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import com.chugyoyo.cosmosagent.mapper.AgentNodeMapper;
import com.chugyoyo.cosmosagent.dto.AgentNodeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentNodeServiceImpl extends ServiceImpl<AgentNodeMapper, AgentNode> implements AgentNodeService {
    
    private final AgentNodeMapper nodeMapper;
    
    @Override
    public List<AgentNodeDTO> getNodesByAgentId(Long agentId) {
        LambdaQueryWrapper<AgentNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentNode::getAgentId, agentId);
        wrapper.orderByAsc(AgentNode::getId);
        
        List<AgentNode> nodes = nodeMapper.selectList(wrapper);
        return nodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgentNodeDTO createNode(AgentNodeDTO dto) {
        AgentNode node = convertToEntity(dto);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        nodeMapper.insert(node);
        return convertToDTO(node);
    }
    
    @Override
    @Transactional
    public AgentNodeDTO updateNode(Long id, AgentNodeDTO dto) {
        AgentNode existing = nodeMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("代理节点不存在");
        }
        
        AgentNode node = convertToEntity(dto);
        node.setId(id);
        node.setAgentId(existing.getAgentId());
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
    public AgentNodeDTO updateNodeYaml(Long id, String yamlConfig) {
        AgentNode node = nodeMapper.selectById(id);
        if (node == null) {
            throw new RuntimeException("代理节点不存在");
        }
        
        node.setYamlConfig(yamlConfig);
        node.setUpdatedAt(LocalDateTime.now());
        nodeMapper.updateById(node);
        return convertToDTO(node);
    }
    
    private AgentNodeDTO convertToDTO(AgentNode entity) {
        AgentNodeDTO dto = new AgentNodeDTO();
        dto.setId(entity.getId());
        dto.setAgentId(entity.getAgentId());
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
    
    private AgentNode convertToEntity(AgentNodeDTO dto) {
        AgentNode entity = new AgentNode();
        entity.setAgentId(dto.getAgentId());
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