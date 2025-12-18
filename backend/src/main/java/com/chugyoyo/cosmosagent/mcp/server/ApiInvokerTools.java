package com.chugyoyo.cosmosagent.mcp.server;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class ApiInvokerTools {

    private final RestTemplate restTemplate;
    // 默认基础 URL，如果在同一个容器或机器，通常是 localhost:8080
    private final String baseUrl = "http://localhost:8080";

    public ApiInvokerTools(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 工具 5: 动态调用项目 API
     * Agent 在理解了接口文档和 DTO 结构后，调用此工具执行业务操作
     */
    @Tool(name = "invoke_api",
            description = "Invoke a REST API endpoint within the project. " +
                    "Requires the path, method, and a JSON body if applicable.")
    public String invokeApi(
            @ToolParam(name = "path", description = "The API path, e.g., '/api/v1/user/create'") String path,
            @ToolParam(name = "method", description = "HTTP Method: GET, POST, PUT, DELETE") String method,
            @ToolParam(name = "jsonBody", description = "The JSON string representation of the request body (DTO). For GET, leave empty.") String jsonBody) {

        log.info("Agent invoking API: {} {} with body: {}", method, path, jsonBody);

        try {
            String url = path.startsWith("http") ? path : baseUrl + path;
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            // 执行调用
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    httpMethod,
                    entity,
                    String.class
            );

            // 返回结果给 Agent，包括状态码，以便 Agent 判断是否成功
            return String.format("Status: %d\nResponse Body: %s",
                    response.getStatusCodeValue(),
                    response.getBody());

        } catch (Exception e) {
            log.error("API Call failed", e);
            // 返回错误详情，Agent 看到 400 可能会检查参数名，看到 500 可能会检查业务逻辑
            return "API Invocation Error: " + e.getMessage();
        }
    }
}