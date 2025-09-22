package com.chugyoyo.cosmosagent.workflow;

import com.chugyoyo.cosmosagent.dto.WorkflowExecutionRequest;
import com.chugyoyo.cosmosagent.dto.WorkflowExecutionResponse;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import com.chugyoyo.cosmosagent.entity.AgentLink;
import com.chugyoyo.cosmosagent.service.AgentLinkService;
import com.chugyoyo.cosmosagent.service.AgentNodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowStateMachine {

    private final AgentNodeService agentNodeService;
    private final AgentLinkService agentLinkService;
    private final NodeExecutorFactory nodeExecutorFactory;
    private final ObjectMapper objectMapper;

    /**
     * 执行工作流
     */
    public WorkflowExecutionResponse executeWorkflow(WorkflowExecutionRequest request) {
        String executionId = UUID.randomUUID().toString().replace("-", "");
        WorkflowContext context = new WorkflowContext(executionId, request);

        try {
            // 从数据库查询节点和连线数据
            List<AgentNode> nodes = agentNodeService.listByAgentId(request.getAgentId());
            List<AgentLink> links = agentLinkService.listByAgentId(request.getAgentId());

            if (nodes.isEmpty()) {
                throw new RuntimeException("代理没有可执行的工作流节点");
            }

            // 验证工作流
            validateWorkflow(nodes, links);

            // 查找开始节点
            AgentNode startNode = findStartNode(nodes);
            if (startNode == null) {
                throw new RuntimeException("工作流必须包含一个开始节点");
            }

            // 执行工作流
            return executeNodes(context, startNode, nodes, links);

        } catch (Exception e) {
            log.error("Workflow execution failed", e);
            context.setState(WorkflowState.FAILED);
            context.setErrorMessage(e.getMessage());

            return WorkflowExecutionResponse.builder()
                    .executionId(executionId)
                    .agentId(request.getAgentId())
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .startTime(context.getStartTime())
                    .endTime(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 验证工作流
     */
    private void validateWorkflow(List<AgentNode> nodes, List<AgentLink> links) {
        if (nodes == null || nodes.isEmpty()) {
            throw new RuntimeException("工作流不能为空");
        }

        // 检查是否有且仅有一个开始节点
        long startNodeCount = nodes.stream()
                .filter(node -> "START".equals(node.getType()))
                .count();
        if (startNodeCount != 1) {
            throw new RuntimeException("工作流必须有且仅有一个开始节点");
        }

        // 验证连线有效性
        for (AgentLink link : links) {
            boolean sourceExists = nodes.stream()
                    .anyMatch(node -> node.getId().equals(link.getSourceNodeId()));
            boolean targetExists = nodes.stream()
                    .anyMatch(node -> node.getId().equals(link.getTargetNodeId()));

            if (!sourceExists || !targetExists) {
                throw new RuntimeException("工作流连线包含无效的节点ID");
            }
        }
    }

    /**
     * 查找开始节点
     */
    private AgentNode findStartNode(List<AgentNode> nodes) {
        return nodes.stream()
                .filter(node -> "START".equals(node.getType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 执行节点
     */
    private WorkflowExecutionResponse executeNodes(WorkflowContext context, AgentNode currentNode, List<AgentNode> nodes, List<AgentLink> links) {
        List<WorkflowExecutionResponse.ExecutionStep> steps = new ArrayList<>();
        Map<String, Object> finalResult = new HashMap<>();
        AgentNode node = currentNode;

        // 执行节点，直到到达结束节点或遇到失败
        while (node != null) {

            context.setCurrentNode(node);
            WorkflowExecutionResponse.ExecutionStep step = executeNode(context, node);
            steps.add(step);

            // 中间失败，熔断
            if (NodeState.FAILED.name().equals(step.getStatus())) {
                return WorkflowExecutionResponse.builder()
                        .executionId(context.getExecutionId())
                        .agentId(context.getRequest().getAgentId())
                        .status(NodeState.FAILED.name())
                        .currentStep(node.getName())
                        .steps(steps)
                        .errorMessage(step.getErrorMessage())
                        .startTime(context.getStartTime())
                        .endTime(LocalDateTime.now())
                        .build();
            }

            // 查找下一个节点
            node = findNextNode(node, nodes, links);
            context.setLastStep(step);
        }

        return WorkflowExecutionResponse.builder()
                .executionId(context.getExecutionId())
                .agentId(context.getRequest().getAgentId())
                .status(NodeState.COMPLETED.name())
                .currentStep(NodeState.COMPLETED.name())
                .steps(steps)
                .result(finalResult)
                .startTime(context.getStartTime())
                .endTime(LocalDateTime.now())
                .build();
    }

    /**
     * 执行单个节点
     */
    private WorkflowExecutionResponse.ExecutionStep executeNode(WorkflowContext context, AgentNode node) {
        LocalDateTime startTime = LocalDateTime.now();
        String stepId = UUID.randomUUID().toString().replace("-", "");

        try {
            log.info("Executing node: {} ({})", node.getName(), node.getType());
            AgentNodeExecutor executor = nodeExecutorFactory.getExecutor(node.getType());
            WorkflowExecutionResponse.ExecutionStep execute = executor.execute(context);
            execute.setStepId(stepId);
            return execute;

        } catch (Exception e) {
            log.error("Node execution failed:{}", node.getName(), e);
            long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
            return new WorkflowExecutionResponse.ExecutionStep(
                    stepId,
                    node.getName(),
                    node.getType(),
                    NodeState.FAILED.name(),
                    context.getLastStep() != null ? context.getLastStep().getOutput() : null,
                    null,
                    e.getMessage(),
                    duration,
                    startTime,
                    LocalDateTime.now()
            );
        }
    }

    /**
     * 查找下一个节点
     */
    private AgentNode findNextNode(AgentNode currentNode, List<AgentNode> nodes, List<AgentLink> links) {
        return links.stream()
                .filter(link -> link.getSourceNodeId().equals(currentNode.getId()))
                .findFirst()
                .map(link -> findNodeById(link.getTargetNodeId(), nodes))
                .orElse(null);
    }

    /**
     * 根据ID查找节点
     */
    private AgentNode findNodeById(Long nodeId, List<AgentNode> nodes) {
        return nodes.stream()
                .filter(node -> node.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
}