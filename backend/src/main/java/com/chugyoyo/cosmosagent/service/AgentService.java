package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AgentDTO;
import java.util.List;

public interface AgentService {
    
    List<AgentDTO> getAllAgents();
    
    AgentDTO getAgentById(Long id);
    
    AgentDTO createAgent(AgentDTO dto);
    
    AgentDTO updateAgent(Long id, AgentDTO dto);
    
    void deleteAgent(Long id);
}