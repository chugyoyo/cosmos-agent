package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AgentOrchestrationNodeDTO;
import java.util.List;

public interface AgentOrchestrationNodeService {
    
    List<AgentOrchestrationNodeDTO> getNodesByOrchestrationId(Long orchestrationId);
    
    AgentOrchestrationNodeDTO getNodeById(Long id);
    
    AgentOrchestrationNodeDTO createNode(AgentOrchestrationNodeDTO dto);
    
    AgentOrchestrationNodeDTO updateNode(Long id, AgentOrchestrationNodeDTO dto);
    
    void deleteNode(Long id);
    
    AgentOrchestrationNodeDTO updateNodeYaml(Long id, String yamlConfig);
}