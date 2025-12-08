package com.chugyoyo.cosmosagent.mcp.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.chugyoyo.cosmosagent.mcp.model.JsonRpcRequest;
import com.chugyoyo.cosmosagent.mcp.model.JsonRpcResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/mcp")
public class McpServerController {

    // 模拟会话存储
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // [端点 1] SSE 连接建立
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30分钟超时
        String sessionId = "session-" + System.currentTimeMillis();
        emitters.put(sessionId, emitter);

        // MCP 规范：连接后通常会发送 endpoint 事件通知客户端可以发送 POST 了
        try {
            emitter.send(SseEmitter.event().name("endpoint").data("/mcp/messages?sessionId=" + sessionId));
            System.out.println("Client connected: " + sessionId);
        } catch (IOException e) {
            emitters.remove(sessionId);
        }
        return emitter;
    }

    // [端点 2] 处理客户端消息 (JSON-RPC)
    @PostMapping("/messages")
    public JsonRpcResponse handleMessage(@RequestParam(required = false) String sessionId,
                                         @RequestBody JsonRpcRequest request) {
        System.out.println("Received Method: " + request.getMethod());

        // 路由逻辑
        switch (request.getMethod()) {
            case "initialize":
                // 握手阶段
                Map<String, Object> initResult = new HashMap<>();
                initResult.put("protocolVersion", "2024-11-05");
                initResult.put("serverInfo", Map.of("name", "MyJavaMcpServer", "version", "1.0"));
                initResult.put("capabilities", Map.of("tools", new HashMap<>()));
                return new JsonRpcResponse("2.0", initResult, null, request.getId());

            case "tools/list":
                // 告诉客户端我有那些工具
                Map<String, Object> tool = new HashMap<>();
                tool.put("name", "add_numbers");
                tool.put("description", "Add two numbers together");
                // 省略复杂的 inputSchema 定义，仅做演示
                return new JsonRpcResponse("2.0", Map.of("tools", java.util.List.of(tool)), null, request.getId());

            case "tools/call":
                // 执行具体的工具逻辑
                return executeTool(request);

            default:
                return new JsonRpcResponse("2.0", null, Map.of("code", -32601, "message", "Method not found"), request.getId());
        }
    }

    // 工具执行逻辑
    private JsonRpcResponse executeTool(JsonRpcRequest request) {
        // 这里为了简化，直接强转 Map，生产环境请用 Jackson ObjectMapper 转换
        Map<String, Object> params = (Map<String, Object>) request.getParams();
        String toolName = (String) params.get("name");
        Map<String, Integer> args = JSON.parseObject(params.get("arguments").toString(), new TypeReference<Map<String, Integer>>() {});

        if ("add_numbers".equals(toolName)) {
            int result = args.get("a") + args.get("b") + 100;
            // MCP 工具返回的标准结构
            Map<String, Object> content = Map.of("type", "text", "text", String.valueOf(result));
            return new JsonRpcResponse("2.0", Map.of("content", java.util.List.of(content)), null, request.getId());
        }

        return new JsonRpcResponse("2.0", null, "Tool not found", request.getId());
    }
}