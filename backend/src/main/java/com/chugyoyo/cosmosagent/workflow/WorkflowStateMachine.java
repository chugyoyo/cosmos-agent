package com.chugyoyo.cosmosagent.workflow;

import com.chugyoyo.cosmosagent.dto.WorkflowExecutionRequest;
import com.chugyoyo.cosmosagent.dto.WorkflowExecutionResponse;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import com.chugyoyo.cosmosagent.entity.AgentLink;
import com.chugyoyo.cosmosagent.mapper.AgentNodeMapper;
import com.chugyoyo.cosmosagent.mapper.AgentLinkMapper;
import com.chugyoyo.cosmosagent.workflow.llm.LLMStrategy;
import com.chugyoyo.cosmosagent.workflow.llm.LLMStrategyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Spring State Machine imports removed - using custom state machine implementation
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowStateMachine {
    
    private final AgentNodeMapper agentNodeMapper;
    private final AgentLinkMapper agentLinkMapper;
    private final LLMStrategyFactory llmStrategyFactory;
    private final ObjectMapper objectMapper;
    
    private final Map<String, WorkflowContext> activeWorkflows = new ConcurrentHashMap<>();
    
    /**
     * 执行工作流
     */
    public WorkflowExecutionResponse executeWorkflow(WorkflowExecutionRequest request) {
        String executionId = UUID.randomUUID().toString();
        WorkflowContext context = new WorkflowContext(executionId, request);
        
        activeWorkflows.put(executionId, context);
        
        try {
            // 从数据库查询节点和连线数据
            List<AgentNode> nodes = agentNodeMapper.findByAgentId(request.getAgentId());
            List<AgentLink> links = agentLinkMapper.findByAgentId(request.getAgentId());
            
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
        } finally {
            activeWorkflows.remove(executionId);
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
        while (node != null) {
            WorkflowExecutionResponse.ExecutionStep step = executeNode(context, node);
            steps.add(step);
            
            if ("FAILED".equals(step.getStatus())) {
                return WorkflowExecutionResponse.builder()
                        .executionId(context.getExecutionId())
                        .agentId(context.getRequest().getAgentId())
                        .status("FAILED")
                        .currentStep(node.getName())
                        .steps(steps)
                        .errorMessage(step.getErrorMessage())
                        .startTime(context.getStartTime())
                        .endTime(LocalDateTime.now())
                        .build();
            }
            
            // 收集结果
            if (step.getOutput() != null) {
                finalResult.putAll(step.getOutput());
                // 将当前节点的输出添加到中间结果中，供后续节点使用
                context.getIntermediateResults().putAll(step.getOutput());
            }
            
            // 查找下一个节点
            node = findNextNode(node, nodes, links);
        }
        
        return WorkflowExecutionResponse.builder()
                .executionId(context.getExecutionId())
                .agentId(context.getRequest().getAgentId())
                .status("COMPLETED")
                .currentStep("Completed")
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
        String stepId = UUID.randomUUID().toString();
        
        try {
            log.info("Executing node: {} ({})", node.getName(), node.getType());
            
            Map<String, Object> input = prepareNodeInput(context, node);
            Map<String, Object> output = null;

            if ("START".equals(node.getType())) {
                output = executeStartNode(context, node, input);
            } else if ("LLM".equals(node.getType())) {
                output = executeLLMNode(context, node, input);
            } else {
                throw new RuntimeException("不支持的节点类型: " + node.getType());
            }
            
            long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
            
            return new WorkflowExecutionResponse.ExecutionStep(
                    stepId,
                    node.getName(),
                    node.getType(),
                    "COMPLETED",
                    input,
                    output,
                    null,
                    duration,
                    startTime,
                    LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Node execution failed:{}",node.getName(), e);
            long duration = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
            
            return new WorkflowExecutionResponse.ExecutionStep(
                    stepId,
                    node.getName(),
                    node.getType(),
                    "FAILED",
                    null,
                    null,
                    e.getMessage(),
                    duration,
                    startTime,
                    LocalDateTime.now()
            );
        }
    }
    
    /**
     * 执行开始节点
     */
    private Map<String, Object> executeStartNode(WorkflowContext context, AgentNode node, Map<String, Object> input) {
        // 开始节点主要是收集输入参数
        Map<String, Object> output = new HashMap<>();
        output.put("nodeType", "START");
        output.put("nodeName", node.getName());
        output.put("timestamp", System.currentTimeMillis());
        
        // 合并用户输入的参数
        if (context.getRequest().getParams() != null) {
            output.putAll(context.getRequest().getParams());
        }
        
        log.info("Start node executed: {}", node.getName());
        return output;
    }
    
    /**
     * 执行LLM节点
     */
    private Map<String, Object> executeLLMNode(WorkflowContext context, AgentNode node, Map<String, Object> input) {
        try {
            // 解析节点配置
            Map<String, Object> llmConfig = objectMapper.readValue(node.getLlmConfig(), Map.class);
            
            if (llmConfig == null) {
                throw new RuntimeException("LLM节点配置不能为空");
            }
            
            // 构建提示词
            String systemPrompt = (String) llmConfig.getOrDefault("systemPrompt", "");
            String userPrompt = (String) llmConfig.getOrDefault("userPrompt", "");
            String model = (String) llmConfig.getOrDefault("model", "glm-4");
            
            // 替换模板变量
            String finalPrompt = buildPrompt(systemPrompt, userPrompt, input);
            
            // 调用LLM策略
            LLMStrategy strategy = llmStrategyFactory.getStrategy("zhipuai");
            Map<String, Object> parameters = Map.of(
                    "model", model,
                    "temperature", llmConfig.getOrDefault("temperature", 0.7),
                    "maxTokens", llmConfig.getOrDefault("maxTokens", 1000)
            );
            
            LLMStrategy.LLMResponse response = strategy.execute(finalPrompt, parameters);
            
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getErrorMessage());
            }
            
            Map<String, Object> output = new HashMap<>();
            output.put("nodeType", "LLM");
            output.put("nodeName", node.getName());
            output.put("model", model);
            output.put("response", response.getContent());
            output.put("text", response.getContent()); // 添加text字段，默认使用LLM响应内容
            output.put("metadata", response.getMetadata());
            output.put("timestamp", System.currentTimeMillis());
            
            log.info("LLM node executed: {}, response length: {}", node.getName(), response.getContent().length());
            return output;
            
        } catch (Exception e) {
            log.error("LLM node execution failed: " + node.getName(), e);
            throw new RuntimeException("LLM节点执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建提示词
     */
    private String buildPrompt(String systemPrompt, String userPrompt, Map<String, Object> input) {
        StringBuilder prompt = new StringBuilder();
        
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            prompt.append("System: ").append(systemPrompt).append("\n\n");
        }
        
        String processedUserPrompt = userPrompt;
        if (input != null) {
            for (Map.Entry<String, Object> entry : input.entrySet()) {
                processedUserPrompt = processedUserPrompt.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }
        
        prompt.append("User: ").append(processedUserPrompt);
        
        return prompt.toString();
    }
    
    /**
     * 准备节点输入
     */
    private Map<String, Object> prepareNodeInput(WorkflowContext context, AgentNode node) {
        Map<String, Object> input = new HashMap<>();
        
        // 如果是开始节点，使用用户输入的参数
        if ("START".equals(node.getType()) && context.getRequest().getParams() != null) {
            input.putAll(context.getRequest().getParams());
        } else {
            // 对于非开始节点，将前面节点的输出作为当前节点的输入
            input.putAll(context.getIntermediateResults());
        }
        
        input.put("nodeId", node.getId());
        input.put("nodeName", node.getName());
        input.put("nodeType", node.getType());
        
        return input;
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
    
    
    /**
     * 工作流上下文
     */
    public static class WorkflowContext {
        private final String executionId;
        private final WorkflowExecutionRequest request;
        private final LocalDateTime startTime;
        private WorkflowState state;
        private String errorMessage;
        private Map<String, Object> intermediateResults;
        
        public WorkflowContext(String executionId, WorkflowExecutionRequest request) {
            this.executionId = executionId;
            this.request = request;
            this.startTime = LocalDateTime.now();
            this.state = WorkflowState.INITIALIZING;
            this.intermediateResults = new HashMap<>();
        }
        
        // Getters and Setters
        public String getExecutionId() { return executionId; }
        public WorkflowExecutionRequest getRequest() { return request; }
        public LocalDateTime getStartTime() { return startTime; }
        public WorkflowState getState() { return state; }
        public void setState(WorkflowState state) { this.state = state; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public Map<String, Object> getIntermediateResults() { return intermediateResults; }
        public void setIntermediateResults(Map<String, Object> intermediateResults) { this.intermediateResults = intermediateResults; }
    }
}