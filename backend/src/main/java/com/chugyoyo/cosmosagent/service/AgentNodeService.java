package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AgentNodeDTO;
import java.util.List;

public interface AgentNodeService {
    
    List<AgentNodeDTO> getNodesByAgentId(Long agentId);

    AgentNodeDTO createNode(AgentNodeDTO dto);
    
    AgentNodeDTO updateNode(Long id, AgentNodeDTO dto);
    
    void deleteNode(Long id);
    
    AgentNodeDTO updateNodeYaml(Long id, String yamlConfig);
}