package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.ApiResp;
import com.chugyoyo.cosmosagent.dto.AgentDTO;
import com.chugyoyo.cosmosagent.dto.AgentNodeDTO;
import com.chugyoyo.cosmosagent.service.AgentService;
import com.chugyoyo.cosmosagent.service.AgentNodeService;
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
    private final AgentNodeService nodeService;

    @GetMapping
    public ApiResp<List<AgentDTO>> getAllAgents() {
        return ApiResp.success(agentService.getAllAgents());
    }

    @GetMapping("/{id}")
    public ApiResp<AgentDTO> getAgentById(@PathVariable Long id) {
        AgentDTO agent = agentService.getAgentById(id);
        return agent != null ? ApiResp.success(agent) : ApiResp.fail("代理不存在");
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

    @PutMapping("/{id}/flow")
    public ApiResp<AgentDTO> updateFlowData(@PathVariable Long id, @RequestBody String flowData) {
        AgentDTO updated = agentService.updateFlowData(id, flowData);
        return ApiResp.success(updated);
    }

    @GetMapping("/{id}/nodes")
    public ApiResp<List<AgentNodeDTO>> getNodesByAgentId(@PathVariable Long id) {
        List<AgentNodeDTO> nodes = nodeService.getNodesByAgentId(id);
        return ApiResp.success(nodes);
    }

    @PostMapping("/{agentId}/nodes")
    public ApiResp<AgentNodeDTO> createNode(@PathVariable Long agentId, @Valid @RequestBody AgentNodeDTO dto) {
        dto.setAgentId(agentId);
        AgentNodeDTO created = nodeService.createNode(dto);
        return ApiResp.success(created);
    }

    @PutMapping("/nodes/{id}")
    public ApiResp<AgentNodeDTO> updateNode(@PathVariable Long id, @Valid @RequestBody AgentNodeDTO dto) {
        AgentNodeDTO updated = nodeService.updateNode(id, dto);
        return ApiResp.success(updated);
    }

    @PutMapping("/nodes/{id}/yaml")
    public ApiResp<AgentNodeDTO> updateNodeYaml(@PathVariable Long id, @RequestBody String yamlConfig) {
        AgentNodeDTO updated = nodeService.updateNodeYaml(id, yamlConfig);
        return ApiResp.success(updated);
    }

    @DeleteMapping("/nodes/{id}")
    public ApiResp<Void> deleteNode(@PathVariable Long id) {
        nodeService.deleteNode(id);
        return ApiResp.success();
    }
}