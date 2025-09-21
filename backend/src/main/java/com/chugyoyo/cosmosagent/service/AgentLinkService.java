package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AgentLinkDTO;
import java.util.List;

public interface AgentLinkService {
    
    /**
     * 根据代理ID获取所有连线
     */
    List<AgentLinkDTO> getLinksByAgentId(Long agentId);
    
    /**
     * 根据连线ID获取连线信息
     */
    AgentLinkDTO getLinkById(Long id);
    
    /**
     * 创建新连线
     */
    AgentLinkDTO createLink(AgentLinkDTO dto);
    
    /**
     * 更新连线信息
     */
    AgentLinkDTO updateLink(Long id, AgentLinkDTO dto);
    
    /**
     * 删除连线
     */
    void deleteLink(Long id);
    
    /**
     * 根据源节点ID获取所有出线连线
     */
    List<AgentLinkDTO> getLinksBySourceNodeId(Long sourceNodeId);
    
    /**
     * 根据目标节点ID获取所有入线连线
     */
    List<AgentLinkDTO> getLinksByTargetNodeId(Long targetNodeId);
    
    /**
     * 根据节点ID获取所有相关连线
     */
    List<AgentLinkDTO> getLinksByNodeId(Long nodeId);
    
    /**
     * 删除代理的所有连线
     */
    void deleteLinksByAgentId(Long agentId);
    
    /**
     * 删除节点的所有相关连线
     */
    void deleteLinksByNodeId(Long nodeId);
    
    /**
     * 检查两个节点之间是否已存在连线
     */
    boolean existsLinkBetweenNodes(Long agentId, Long sourceNodeId, Long targetNodeId);
}