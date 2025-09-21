package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.dto.AgentLinkDTO;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import com.chugyoyo.cosmosagent.mapper.AgentNodeMapper;
import com.chugyoyo.cosmosagent.dto.AgentNodeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentNodeServiceImpl extends ServiceImpl<AgentNodeMapper, AgentNode> implements AgentNodeService {
    
    private final AgentNodeMapper agentNodeMapper;
    
    @Override
    public List<AgentNodeDTO> getNodesByAgentId(Long agentId) {
        LambdaQueryWrapper<AgentNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentNode::getAgentId, agentId);
        wrapper.orderByAsc(AgentNode::getId);
        
        List<AgentNode> nodes = agentNodeMapper.selectList(wrapper);
        return nodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgentNodeDTO saveUpdateNode(AgentNodeDTO dto) {
        AgentNode node = convertToEntity(dto);
        node.setUpdatedAt(LocalDateTime.now());
        saveOrUpdate(node);
        return convertToDTO(node);
    }

    @Override
    @Transactional
    public void deleteNode(Long id) {
        agentNodeMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public AgentNodeDTO updateNodeYaml(Long id, String yamlConfig) {
        AgentNode node = agentNodeMapper.selectById(id);
        if (node == null) {
            throw new RuntimeException("代理节点不存在");
        }
        
        node.setYamlConfig(yamlConfig);
        node.setUpdatedAt(LocalDateTime.now());
        agentNodeMapper.updateById(node);
        return convertToDTO(node);
    }

    @Override
    public void saveUpdateLink(AgentLinkDTO dto) {
        // TODo 数据库
    }

    private AgentNodeDTO convertToDTO(AgentNode entity) {
        AgentNodeDTO dto = new AgentNodeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private AgentNode convertToEntity(AgentNodeDTO dto) {
        AgentNode entity = new AgentNode();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}