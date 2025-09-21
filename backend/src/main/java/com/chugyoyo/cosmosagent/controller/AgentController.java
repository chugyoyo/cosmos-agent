package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.ApiResp;
import com.chugyoyo.cosmosagent.dto.AgentDTO;
import com.chugyoyo.cosmosagent.dto.AgentLinkDTO;
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

    @GetMapping("/getAllAgents")
    public ApiResp<List<AgentDTO>> getAllAgents() {
        return ApiResp.success(agentService.getAllAgents());
    }

    @GetMapping("/{id}/getAgentById")
    public ApiResp<AgentDTO> getAgentById(@PathVariable Long id) {
        AgentDTO agent = agentService.getAgentById(id);
        return agent != null ? ApiResp.success(agent) : ApiResp.fail("代理不存在");
    }

    @PostMapping("/createAgent")
    public ApiResp<AgentDTO> createAgent(@Valid @RequestBody AgentDTO dto) {
        AgentDTO created = agentService.createAgent(dto);
        return ApiResp.success(created);
    }

    @PutMapping("/{id}/updateAgent")
    public ApiResp<AgentDTO> updateAgent(@PathVariable Long id, @Valid @RequestBody AgentDTO dto) {
        AgentDTO updated = agentService.updateAgent(id, dto);
        return ApiResp.success(updated);
    }

    @DeleteMapping("/{id}/deleteAgent")
    public ApiResp<Void> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ApiResp.success();
    }

    @GetMapping("/getNodesByAgentId")
    public ApiResp<List<AgentNodeDTO>> getNodesByAgentId(@RequestParam Long id) {
        List<AgentNodeDTO> nodes = nodeService.getNodesByAgentId(id);
        return ApiResp.success(nodes);
    }

    @PostMapping("/agentId/{agentId}/saveUpdateNode")
    public ApiResp<AgentNodeDTO> saveUpdateNode(@PathVariable Long agentId, @Valid @RequestBody AgentNodeDTO dto) {
        dto.setAgentId(agentId);
        AgentNodeDTO created = nodeService.saveUpdateNode(dto);
        return ApiResp.success(created);
    }

    @PutMapping("/nodes/{id}/updateNodeYaml")
    public ApiResp<AgentNodeDTO> updateNodeYaml(@PathVariable Long id, @RequestBody String yamlConfig) {
        AgentNodeDTO updated = nodeService.updateNodeYaml(id, yamlConfig);
        return ApiResp.success(updated);
    }

    @DeleteMapping("/nodes/{id}/deleteNode")
    public ApiResp<Void> deleteNode(@PathVariable Long id) {
        nodeService.deleteNode(id);
        return ApiResp.success();
    }

    // saveUpdateLink
    @PostMapping("/saveUpdateLink")
    public ApiResp<Void> saveUpdateLink(@Valid @RequestBody AgentLinkDTO dto) {
        nodeService.saveUpdateLink(dto);
        return ApiResp.success();
    }
}