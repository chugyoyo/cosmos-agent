package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.dto.AgentDTO;
import com.chugyoyo.cosmosagent.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgentController {
    
    private final AgentService agentService;
    
    @GetMapping
    public ResponseEntity<List<AgentDTO>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AgentDTO>> getAgentsByType(@PathVariable String type) {
        return ResponseEntity.ok(agentService.getAgentsByType(type));
    }
    
    @GetMapping("/deployed")
    public ResponseEntity<List<AgentDTO>> getDeployedAgents() {
        return ResponseEntity.ok(agentService.getDeployedAgents());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AgentDTO> getAgentById(@PathVariable Long id) {
        AgentDTO agent = agentService.getAgentById(id);
        return agent != null ? ResponseEntity.ok(agent) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<AgentDTO> createAgent(@Valid @RequestBody AgentDTO dto) {
        AgentDTO created = agentService.createAgent(dto);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AgentDTO> updateAgent(@PathVariable Long id, @Valid @RequestBody AgentDTO dto) {
        try {
            AgentDTO updated = agentService.updateAgent(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/deploy")
    public ResponseEntity<AgentDTO> deployAgent(@PathVariable Long id) {
        try {
            AgentDTO deployed = agentService.deployAgent(id);
            return ResponseEntity.ok(deployed);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/undeploy")
    public ResponseEntity<AgentDTO> undeployAgent(@PathVariable Long id) {
        try {
            AgentDTO undeployed = agentService.undeployAgent(id);
            return ResponseEntity.ok(undeployed);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/call")
    public ResponseEntity<AgentDTO> incrementCallCount(@PathVariable Long id) {
        try {
            AgentDTO agent = agentService.incrementCallCount(id);
            return ResponseEntity.ok(agent);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}