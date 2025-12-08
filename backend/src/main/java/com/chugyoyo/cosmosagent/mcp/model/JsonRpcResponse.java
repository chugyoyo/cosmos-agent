package com.chugyoyo.cosmosagent.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonRpcResponse {
    private String jsonrpc = "2.0";
    private Object result;
    private Object error;
    private Object id;
}