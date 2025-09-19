package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.ApiResp;
import com.chugyoyo.cosmosagent.dto.AgentOrchestrationDTO;
import com.chugyoyo.cosmosagent.dto.AgentOrchestrationNodeDTO;
import com.chugyoyo.cosmosagent.service.AgentOrchestrationService;
import com.chugyoyo.cosmosagent.service.AgentOrchestrationNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/orchestrations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgentOrchestrationController {

    private final AgentOrchestrationService orchestrationService;
    private final AgentOrchestrationNodeService nodeService;
    private final AgentOrchestrationService agentOrchestrationService;

    @GetMapping
    public ApiResp<List<AgentOrchestrationDTO>> getAllOrchestrations() {
        return ApiResp.success(orchestrationService.getAllOrchestrations());
    }

    @GetMapping("/{id}")
    public ApiResp<AgentOrchestrationDTO> getOrchestrationById(@PathVariable Long id) {
        AgentOrchestrationDTO orchestration = orchestrationService.getOrchestrationById(id);
        return orchestration != null ? ApiResp.success(orchestration) : ApiResp.fail("编排不存在");
    }

    @PostMapping
    public ApiResp<AgentOrchestrationDTO> createOrchestration(@Valid @RequestBody AgentOrchestrationDTO dto) {
        AgentOrchestrationDTO created = orchestrationService.createOrchestration(dto);
        return ApiResp.success(created);
    }

    @PutMapping("/{id}")
    public ApiResp<AgentOrchestrationDTO> updateOrchestration(@PathVariable Long id, @Valid @RequestBody AgentOrchestrationDTO dto) {
        AgentOrchestrationDTO updated = orchestrationService.updateOrchestration(id, dto);
        return ApiResp.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResp<Void> deleteOrchestration(@PathVariable Long id) {
        orchestrationService.deleteOrchestration(id);
        return ApiResp.success();
    }

    @PutMapping("/{id}/flow")
    public ApiResp<AgentOrchestrationDTO> updateFlowData(@PathVariable Long id, @RequestBody String flowData) {
        AgentOrchestrationDTO updated = orchestrationService.updateFlowData(id, flowData);
        return ApiResp.success(updated);
    }

    @GetMapping("/{id}/nodes")
    public ApiResp<List<AgentOrchestrationNodeDTO>> getNodesByOrchestrationId(@PathVariable Long id) {
        List<AgentOrchestrationNodeDTO> nodes = nodeService.getNodesByOrchestrationId(id);
        return ApiResp.success(nodes);
    }

    @PostMapping("/{orchestrationId}/nodes")
    public ApiResp<AgentOrchestrationNodeDTO> createNode(@PathVariable Long orchestrationId, @Valid @RequestBody AgentOrchestrationNodeDTO dto) {
        dto.setOrchestrationId(orchestrationId);
        AgentOrchestrationNodeDTO created = nodeService.createNode(dto);
        return ApiResp.success(created);
    }

    @PutMapping("/nodes/{id}")
    public ApiResp<AgentOrchestrationNodeDTO> updateNode(@PathVariable Long id, @Valid @RequestBody AgentOrchestrationNodeDTO dto) {
        AgentOrchestrationNodeDTO updated = nodeService.updateNode(id, dto);
        return ApiResp.success(updated);
    }

    @PutMapping("/nodes/{id}/yaml")
    public ApiResp<AgentOrchestrationNodeDTO> updateNodeYaml(@PathVariable Long id, @RequestBody String yamlConfig) {
        AgentOrchestrationNodeDTO updated = nodeService.updateNodeYaml(id, yamlConfig);
        return ApiResp.success(updated);
    }

    @DeleteMapping("/nodes/{id}")
    public ApiResp<Void> deleteNode(@PathVariable Long id) {
        nodeService.deleteNode(id);
        return ApiResp.success();
    }
}