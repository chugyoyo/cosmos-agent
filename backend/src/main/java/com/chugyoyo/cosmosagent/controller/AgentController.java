package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.ApiResp;
import com.chugyoyo.cosmosagent.dto.AgentDTO;
import com.chugyoyo.cosmosagent.dto.AgentLinkDTO;
import com.chugyoyo.cosmosagent.dto.AgentNodeDTO;
import com.chugyoyo.cosmosagent.dto.WorkflowExecutionRequest;
import com.chugyoyo.cosmosagent.dto.WorkflowExecutionResponse;
import com.chugyoyo.cosmosagent.service.AgentService;
import com.chugyoyo.cosmosagent.service.AgentNodeService;
import com.chugyoyo.cosmosagent.service.AgentLinkService;
import com.chugyoyo.cosmosagent.workflow.WorkflowStateMachine;
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
    private final AgentLinkService linkService;
    private final WorkflowStateMachine workflowStateMachine;

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

    @DeleteMapping("/deleteNode")
    public ApiResp<Void> deleteNode(@RequestParam("nodeId") Long id) {
        nodeService.deleteNode(id);
        return ApiResp.success();
    }

    // ===== Link 相关 API =====
    
    @GetMapping("/getLinksByAgentId")
    public ApiResp<List<AgentLinkDTO>> getLinksByAgentId(@RequestParam Long id) {
        List<AgentLinkDTO> links = linkService.getLinksByAgentId(id);
        return ApiResp.success(links);
    }
    
    @GetMapping("/links/{id}")
    public ApiResp<AgentLinkDTO> getLinkById(@PathVariable Long id) {
        AgentLinkDTO link = linkService.getLinkById(id);
        return link != null ? ApiResp.success(link) : ApiResp.fail("连线不存在");
    }
    
    @PostMapping("/saveUpdateLink")
    public ApiResp<AgentLinkDTO> saveUpdateLink(@Valid @RequestBody AgentLinkDTO dto) {
        if (dto.getId() != null) {
            // 更新现有连线
            AgentLinkDTO updated = linkService.updateLink(dto.getId(), dto);
            return ApiResp.success(updated);
        } else {
            // 创建新连线
            AgentLinkDTO created = linkService.createLink(dto);
            return ApiResp.success(created);
        }
    }
    
    @PutMapping("/links/{id}")
    public ApiResp<AgentLinkDTO> updateLink(@PathVariable Long id, @Valid @RequestBody AgentLinkDTO dto) {
        AgentLinkDTO updated = linkService.updateLink(id, dto);
        return ApiResp.success(updated);
    }
    
    @DeleteMapping("/links/{id}")
    public ApiResp<Void> deleteLink(@PathVariable Long id) {
        linkService.deleteLink(id);
        return ApiResp.success();
    }
    
    @GetMapping("/nodes/{nodeId}/sourceLinks")
    public ApiResp<List<AgentLinkDTO>> getLinksBySourceNodeId(@PathVariable Long nodeId) {
        List<AgentLinkDTO> links = linkService.getLinksBySourceNodeId(nodeId);
        return ApiResp.success(links);
    }
    
    @GetMapping("/nodes/{nodeId}/targetLinks")
    public ApiResp<List<AgentLinkDTO>> getLinksByTargetNodeId(@PathVariable Long nodeId) {
        List<AgentLinkDTO> links = linkService.getLinksByTargetNodeId(nodeId);
        return ApiResp.success(links);
    }
    
    @GetMapping("/nodes/{nodeId}/allLinks")
    public ApiResp<List<AgentLinkDTO>> getLinksByNodeId(@PathVariable Long nodeId) {
        List<AgentLinkDTO> links = linkService.getLinksByNodeId(nodeId);
        return ApiResp.success(links);
    }
    
    @GetMapping("/checkLinkExists")
    public ApiResp<Boolean> checkLinkExists(@RequestParam Long agentId, 
                                           @RequestParam Long sourceNodeId, 
                                           @RequestParam Long targetNodeId) {
        boolean exists = linkService.existsLinkBetweenNodes(agentId, sourceNodeId, targetNodeId);
        return ApiResp.success(exists);
    }
    
    // ===== 工作流执行相关 API =====
    
    @PostMapping("/executeWorkflow")
    public ApiResp<WorkflowExecutionResponse> executeWorkflow(@Valid @RequestBody WorkflowExecutionRequest request) {
        try {
            WorkflowExecutionResponse response = workflowStateMachine.executeWorkflow(request);
            return ApiResp.success(response);
        } catch (Exception e) {
            return ApiResp.fail("工作流执行失败: " + e.getMessage());
        }
    }
}