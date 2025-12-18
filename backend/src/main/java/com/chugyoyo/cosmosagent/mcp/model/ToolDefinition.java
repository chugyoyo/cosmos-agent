package com.chugyoyo.cosmosagent.mcp.model;
import lombok.Builder;
import lombok.Data;


// 工具定义的元数据（发给 LLM 看的 Schema）
@Data @Builder
public class ToolDefinition {
    private String name;
    private String description;
    private String jsonSchema; // 参数的 JSON Schema 描述
}