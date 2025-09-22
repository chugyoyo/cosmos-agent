package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AgentLinkDTO;
import com.chugyoyo.cosmosagent.dto.AgentNodeDTO;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface AgentNodeService {
    
    List<AgentNodeDTO> getNodesByAgentId(Long agentId);

    AgentNodeDTO saveUpdateNode(AgentNodeDTO dto);

    void deleteNode(Long id);

    void saveUpdateLink(AgentLinkDTO dto);

    List<AgentNode> listByAgentId(Long agentId);
}