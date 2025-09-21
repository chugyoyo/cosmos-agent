package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AgentLinkDTO;
import com.chugyoyo.cosmosagent.dto.AgentNodeDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AgentNodeService {
    
    List<AgentNodeDTO> getNodesByAgentId(Long agentId);

    AgentNodeDTO saveUpdateNode(AgentNodeDTO dto);

    void deleteNode(Long id);
    
    AgentNodeDTO updateNodeYaml(Long id, String yamlConfig);

    void saveUpdateLink(AgentLinkDTO dto);
}