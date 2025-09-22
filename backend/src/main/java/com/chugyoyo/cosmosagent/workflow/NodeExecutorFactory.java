package com.chugyoyo.cosmosagent.workflow;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 节点执行器工厂
 * 根据节点类型提供相应的执行器
 */
@Slf4j
@Component
public class NodeExecutorFactory {

    private final Map<NodeType, AgentNodeExecutor> executorMap = new EnumMap<>(NodeType.class);

    @Autowired
    public NodeExecutorFactory(List<AgentNodeExecutor> executors) {
        // Spring会自动注入所有AgentNodeExecutor的实现
        registerExecutors(executors);
    }

    @PostConstruct
    public void init() {
        log.info("NodeExecutorFactory initialized with {} executors: {}", 
                executorMap.size(), executorMap.keySet());
    }

    /**
     * 注册执行器
     */
    private void registerExecutors(List<AgentNodeExecutor> executors) {
        for (AgentNodeExecutor executor : executors) {
            NodeType nodeType = executor.getSupportedNodeType();
            if (executorMap.containsKey(nodeType)) {
                log.warn("Duplicate executor found for node type: {}, replacing", nodeType);
            }
            executorMap.put(nodeType, executor);
            log.info("Registered executor {} for node type {}", 
                    executor.getClass().getSimpleName(), nodeType);
        }
    }

    /**
     * 获取节点执行器
     * 
     * @param nodeType 节点类型
     * @return 对应的执行器
     * @throws IllegalArgumentException 如果不支持的节点类型
     */
    public AgentNodeExecutor getExecutor(NodeType nodeType) {
        AgentNodeExecutor executor = executorMap.get(nodeType);
        if (executor == null) {
            throw new IllegalArgumentException("Unsupported node type: " + nodeType);
        }
        return executor;
    }

    /**
     * 获取节点执行器（通过节点类型字符串）
     * 
     * @param nodeType 节点类型字符串
     * @return 对应的执行器
     * @throws IllegalArgumentException 如果不支持的节点类型
     */
    public AgentNodeExecutor getExecutor(String nodeType) {
        try {
            NodeType type = NodeType.valueOf(nodeType);
            return getExecutor(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported node type: " + nodeType, e);
        }
    }
}