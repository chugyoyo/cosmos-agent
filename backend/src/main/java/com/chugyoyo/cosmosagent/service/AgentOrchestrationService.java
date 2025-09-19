package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AgentOrchestrationDTO;
import java.util.List;

public interface AgentOrchestrationService {
    
    List<AgentOrchestrationDTO> getAllOrchestrations();
    
    AgentOrchestrationDTO getOrchestrationById(Long id);
    
    AgentOrchestrationDTO createOrchestration(AgentOrchestrationDTO dto);
    
    AgentOrchestrationDTO updateOrchestration(Long id, AgentOrchestrationDTO dto);
    
    void deleteOrchestration(Long id);
    
    AgentOrchestrationDTO updateFlowData(Long id, String flowData);
}