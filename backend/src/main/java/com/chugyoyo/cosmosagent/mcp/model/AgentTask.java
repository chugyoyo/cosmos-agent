package com.chugyoyo.cosmosagent.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentTask {
    private String taskId;
    private String taskName;
    private String goal;       // 核心目标
    private Map<String, Object> context; // 存放任务所需的上下文数据（如 API 路径、表名等）
    private List<String> requiredTools; // 建议该子 Agent 启用的工具
}
