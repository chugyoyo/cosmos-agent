package com.chugyoyo.cosmosagent.mcp.model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolRegistry {

    // 存储工具名称 -> 执行逻辑的映射
    private final Map<String, ToolExecutor> tools = new ConcurrentHashMap<>();
    private final Map<String, ToolDefinition> toolDefinitions = new ConcurrentHashMap<>();

    public void register(String name, ToolDefinition def, ToolExecutor executor) {
        tools.put(name, executor);
        toolDefinitions.put(name, def);
    }

    public ToolExecutor getExecutor(String name) {
        return tools.get(name);
    }

    public List<ToolDefinition> getAllDefinitions() {
        return new ArrayList<>(toolDefinitions.values());
    }

    // 函数式接口，用于执行工具
    @FunctionalInterface
    public interface ToolExecutor {
        String execute(Map<String, Object> args) throws Exception;
    }
}