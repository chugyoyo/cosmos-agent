package com.chugyoyo.cosmosagent.workflow;

import com.chugyoyo.cosmosagent.dto.WorkflowExecutionResponse;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * START节点执行器
 * 负责初始化工作流参数和收集用户输入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartNodeExecutor implements AgentNodeExecutor {

    private final ObjectMapper objectMapper;

    @Override
    public WorkflowExecutionResponse.ExecutionStep execute(WorkflowContext context) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        AgentNode currentNode = context.getCurrentNode();

        log.info("Executing START node: {}", currentNode.getName());

        LocalDateTime endTime = LocalDateTime.now();
        long executionTime = Duration.between(startTime, endTime).toMillis();
        log.info("START node executed successfully: {}, execution time: {}ms", currentNode.getName(), executionTime);

        return WorkflowExecutionResponse.ExecutionStep.builder()
                .output(context.getRequest().getParams())
                .input(context.getRequest().getParams())
                .nodeName(currentNode.getName())
                .nodeType(NodeType.START.name())
                .status(WorkflowState.COMPLETED.name())
                .startTime(startTime)
                .endTime(endTime)
                .duration(executionTime)
                .build();
    }

    @Override
    public NodeType getSupportedNodeType() {
        return NodeType.START;
    }
}