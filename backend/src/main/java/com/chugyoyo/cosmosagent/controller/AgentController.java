package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.HttpStatus;
import com.chugyoyo.cosmosagent.dto.AgentDTO;
import com.chugyoyo.cosmosagent.service.AgentService;
import com.chugyoyo.cosmosagent.common.ApiResp;
import lombok.RequiredArgsConstructor;
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
    public ApiResp<List<AgentDTO>> getAllAgents() {
        return ApiResp.success(agentService.getAllAgents());
    }

    @GetMapping("/type/{type}")
    public ApiResp<List<AgentDTO>> getAgentsByType(@PathVariable String type) {
        return ApiResp.success(agentService.getAgentsByType(type));
    }

    @GetMapping("/deployed")
    public ApiResp<List<AgentDTO>> getDeployedAgents() {
        return ApiResp.success(agentService.getDeployedAgents());
    }

    @GetMapping("/{id}")
    public ApiResp<AgentDTO> getAgentById(@PathVariable Long id) {
        AgentDTO agent = agentService.getAgentById(id);
        return agent != null ? ApiResp.success(agent) : ApiResp.fail(HttpStatus.NOT_FOUND, "Agent not found");
    }

    @PostMapping
    public ApiResp<AgentDTO> createAgent(@Valid @RequestBody AgentDTO dto) {
        AgentDTO created = agentService.createAgent(dto);
        return ApiResp.success(created);
    }

    @PutMapping("/{id}")
    public ApiResp<AgentDTO> updateAgent(@PathVariable Long id, @Valid @RequestBody AgentDTO dto) {
        AgentDTO updated = agentService.updateAgent(id, dto);
        return ApiResp.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResp<Void> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ApiResp.success();
    }

    @PostMapping("/{id}/deploy")
    public ApiResp<AgentDTO> deployAgent(@PathVariable Long id) {
        AgentDTO deployed = agentService.deployAgent(id);
        return ApiResp.success(deployed);
    }

    @PostMapping("/{id}/undeploy")
    public ApiResp<AgentDTO> undeployAgent(@PathVariable Long id) {
        AgentDTO undeployed = agentService.undeployAgent(id);
        return ApiResp.success(undeployed);
    }

    @PostMapping("/{id}/call")
    public ApiResp<AgentDTO> incrementCallCount(@PathVariable Long id) {
        AgentDTO agent = agentService.incrementCallCount(id);
        return ApiResp.success(agent);
    }
}