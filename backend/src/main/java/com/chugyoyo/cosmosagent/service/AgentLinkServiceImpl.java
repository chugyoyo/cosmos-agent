package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chugyoyo.cosmosagent.dto.AgentLinkDTO;
import com.chugyoyo.cosmosagent.entity.AgentLink;
import com.chugyoyo.cosmosagent.mapper.AgentLinkMapper;
import com.chugyoyo.cosmosagent.mapper.AgentNodeMapper;
import com.chugyoyo.cosmosagent.mapper.AgentMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentLinkServiceImpl implements AgentLinkService {
    
    @Autowired
    private AgentLinkMapper agentLinkMapper;
    
    @Autowired
    private AgentNodeMapper agentNodeMapper;
    
    @Autowired
    private AgentMapper agentMapper;
    
    @Override
    public List<AgentLinkDTO> getLinksByAgentId(Long agentId) {
        List<AgentLink> links = agentLinkMapper.findByAgentId(agentId);
        return links.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AgentLinkDTO getLinkById(Long id) {
        AgentLink link = agentLinkMapper.selectById(id);
        return link != null ? convertToDTO(link) : null;
    }
    
    @Override
    @Transactional
    public AgentLinkDTO createLink(AgentLinkDTO dto) {
        // 验证代理存在
        if (agentMapper.selectById(dto.getAgentId()) == null) {
            throw new RuntimeException("代理不存在");
        }
        
        // 验证源节点存在
        if (agentNodeMapper.selectById(dto.getSourceNodeId()) == null) {
            throw new RuntimeException("源节点不存在");
        }
        
        // 验证目标节点存在
        if (agentNodeMapper.selectById(dto.getTargetNodeId()) == null) {
            throw new RuntimeException("目标节点不存在");
        }
        
        // 检查是否已存在相同连线
        if (existsLinkBetweenNodes(dto.getAgentId(), dto.getSourceNodeId(), dto.getTargetNodeId())) {
            throw new RuntimeException("两个节点之间已存在连线");
        }
        
        // 不能连接到自身
        if (dto.getSourceNodeId().equals(dto.getTargetNodeId())) {
            throw new RuntimeException("不能连接到自身");
        }
        
        AgentLink link = convertToEntity(dto);
        link.setCreatedAt(LocalDateTime.now());
        link.setUpdatedAt(LocalDateTime.now());
        link.setStatus(1); // 默认启用状态
        
        agentLinkMapper.insert(link);
        return convertToDTO(link);
    }
    
    @Override
    @Transactional
    public AgentLinkDTO updateLink(Long id, AgentLinkDTO dto) {
        AgentLink existingLink = agentLinkMapper.selectById(id);
        if (existingLink == null) {
            throw new RuntimeException("连线不存在");
        }
        
        // 更新字段
        BeanUtils.copyProperties(dto, existingLink, "id", "agentId", "sourceNodeId", "targetNodeId", "createdAt");
        existingLink.setUpdatedAt(LocalDateTime.now());
        
        agentLinkMapper.updateById(existingLink);
        return convertToDTO(existingLink);
    }
    
    @Override
    @Transactional
    public void deleteLink(Long id) {
        AgentLink link = agentLinkMapper.selectById(id);
        if (link != null) {
            agentLinkMapper.deleteById(id);
        }
    }
    
    @Override
    public List<AgentLinkDTO> getLinksBySourceNodeId(Long sourceNodeId) {
        List<AgentLink> links = agentLinkMapper.findBySourceNodeId(sourceNodeId);
        return links.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AgentLinkDTO> getLinksByTargetNodeId(Long targetNodeId) {
        List<AgentLink> links = agentLinkMapper.findByTargetNodeId(targetNodeId);
        return links.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AgentLinkDTO> getLinksByNodeId(Long nodeId) {
        List<AgentLink> links = agentLinkMapper.findByNodeId(nodeId);
        return links.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteLinksByAgentId(Long agentId) {
        agentLinkMapper.deleteByAgentId(agentId);
    }
    
    @Override
    public void deleteLinksByNodeId(Long nodeId) {
        agentLinkMapper.deleteByNodeId(nodeId);
    }
    
    @Override
    public boolean existsLinkBetweenNodes(Long agentId, Long sourceNodeId, Long targetNodeId) {
        return agentLinkMapper.countByNodes(agentId, sourceNodeId, targetNodeId) > 0;
    }

    @Override
    public List<AgentLink> listByAgentId(Long agentId) {
        LambdaQueryWrapper<AgentLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentLink::getAgentId, agentId);
        return agentLinkMapper.selectList(wrapper);
    }

    private AgentLinkDTO convertToDTO(AgentLink link) {
        AgentLinkDTO dto = new AgentLinkDTO();
        BeanUtils.copyProperties(link, dto);
        return dto;
    }
    
    private AgentLink convertToEntity(AgentLinkDTO dto) {
        AgentLink link = new AgentLink();
        BeanUtils.copyProperties(dto, link);
        return link;
    }
}