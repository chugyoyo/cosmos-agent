package com.chugyoyo.cosmosagent.mcp.service;

import com.chugyoyo.cosmosagent.mcp.model.JsonRpcRequest;
import com.chugyoyo.cosmosagent.mcp.model.JsonRpcResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
import java.util.Map;

@Service
public class McpClientService {

    private final RestClient restClient;
    private final String SERVER_URL = "http://localhost:8080/mcp";

    public McpClientService(RestClient.Builder builder) {
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
        return restClient.post()
                .uri("/messages")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonRpcResponse.class);
    }
}
