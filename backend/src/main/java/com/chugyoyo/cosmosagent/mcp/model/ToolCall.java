package com.chugyoyo.cosmosagent.mcp.model;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data @Builder
public class ToolCall {
    private String id;
    private String functionName;
    private Map<String, Object> arguments;
}