package com.chugyoyo.cosmosagent.mcp.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 1. 基础 JSON-RPC 请求
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonRpcRequest {
    private String jsonrpc = "2.0";
    private String method;
    private Object params; // 可以是 Map 或自定义对象
    private Object id;
}