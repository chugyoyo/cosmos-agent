package com.chugyoyo.cosmosagent.mcp.client;

import com.alibaba.fastjson2.JSONObject;
import com.chugyoyo.cosmosagent.mcp.model.JsonRpcRequest;
import com.chugyoyo.cosmosagent.mcp.model.JsonRpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class McpClient {

    private final RestClient restClient;
    private final String SERVER_URL = "http://localhost:8080/mcp";

    public McpClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(SERVER_URL).build();
    }

    public void runDemo() {
        System.out.println("--- Starting MCP Client Demo ---");

        // 1. (可选) 实际场景中应先连接 SSE 监听 /sse 事件，
        // 但对于 HTTP POST 这种 Request/Response 模型，我们直接发 POST 也能通。

        // 2. 发送 Initialize (握手)
        JsonRpcRequest initReq = new JsonRpcRequest("2.0", "initialize",
                Map.of("clientInfo", Map.of("name", "JavaClient", "version", "1.0")), 1);

        JsonRpcResponse initResp = sendPost(initReq);
        System.out.println("[Init] Server Protocol Version: " +
                ((Map)initResp.getResult()).get("protocolVersion"));

        // 3. 查询可用工具 (tools/list)
        JsonRpcRequest listReq = new JsonRpcRequest("2.0", "tools/list", null, 2);
        JsonRpcResponse listResp = sendPost(listReq);
        System.out.println("[List] Available Tools: " + listResp.getResult());

        // 4. 调用工具 (tools/call -> add_numbers)
        Map<String, Object> callParams = new HashMap<>();
        callParams.put("name", "add_numbers");
        callParams.put("arguments", Map.of("a", 10, "b", 25));

        JsonRpcRequest callReq = new JsonRpcRequest("2.0", "tools/call", callParams, 3);
        JsonRpcResponse callResp = sendPost(callReq);

        System.out.println("[Call] Result: " + callResp.getResult());
    }

    private JsonRpcResponse sendPost(JsonRpcRequest request) {
        log.info("Sending POST request to {} with payload: {}", SERVER_URL, request);
        return restClient.post()
                .uri("/messages")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonRpcResponse.class);
    }

    // 获取 MCP 工具并转换为 OpenAI 格式
    public List<Map<String, Object>> fetchAndConvertMcpTools() {
        // 请求 MCP Server 的 tools/list
        JsonRpcRequest listReq = new JsonRpcRequest("2.0", "tools/list", null, 1);
        JsonRpcResponse resp = sendPost(listReq);

        // 解析并转换 (这里简化处理，实际需要深层 JSON Schema 转换)
        Map result = (Map) resp.getResult();
        List<Map> mcpTools = (List<Map>) result.get("tools");

        List<Map<String, Object>> openAiTools = new ArrayList<>();
        for (Map mcpTool : mcpTools) {
            Map<String, Object> func = new HashMap<>();
            func.put("name", mcpTool.get("name"));
            func.put("description", mcpTool.get("description"));
            // MCP 的 inputSchema 通常兼容 JSON Schema，可以直接作为 parameters
            // 这是一个假设，实际可能需要微调字段
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("type", "object");
            parameters.put("properties", Map.of(
                    "a", Map.of("type", "integer"),
                    "b", Map.of("type", "integer")
            ));
            func.put("parameters", parameters);

            openAiTools.add(Map.of(
                    "type", "function",
                    "function", func
            ));
        }
        return openAiTools;
    }

    public String executeMcpTool(String toolName, String argsJson) {
        Map<String, Object> callParams = new HashMap<>();
        callParams.put("name", toolName);
        callParams.put("arguments", argsJson);

        JsonRpcRequest callReq = new JsonRpcRequest("2.0", "tools/call", callParams, 3);
        JsonRpcResponse callResp = sendPost(callReq);

        System.out.println("[Call] Result: " + callResp.getResult());
        Object result = callResp.getResult();
        JSONObject resultJson = JSONObject.parseObject(JSONObject.toJSONString(result));
        return resultJson.getJSONArray("content").getJSONObject(0).getString("text");
    }
}
