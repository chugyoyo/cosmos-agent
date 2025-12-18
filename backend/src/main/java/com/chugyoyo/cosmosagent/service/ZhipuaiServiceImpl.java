package com.chugyoyo.cosmosagent.service;

import com.alibaba.fastjson2.JSON;
import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.mcp.client.McpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZhipuaiServiceImpl implements ZhipuaiService {

    @Autowired
    @Lazy
    private AIConfigurationService configurationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private McpClient mcpClient;

    @Override
    public Flux<String> streamChat(String message) {
        try {
            // 获取智谱AI配置
            AIConfigurationDTO config = configurationService.getConfigurationByProvider("zhipuai");
            if (config == null || !StringUtils.hasText(config.getApiKey())) {
                return Flux.error(new RuntimeException("智谱AI配置未找到或API Key未配置"));
            }

            String baseUrl = StringUtils.hasText(config.getBaseUrl()) ? config.getBaseUrl() : "https://open.bigmodel.cn/api/paas/v4";
            String model = StringUtils.hasText(config.getModel()) ? config.getModel() : "glm-4";

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);

            // 构建消息
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            requestBody.put("messages", new Object[]{userMessage});

            // 创建WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            log.info("开始调用智谱AI LangChain 流式聊天，模型: {}", model);

            return webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<String>() {
                    })
                    .timeout(Duration.ofSeconds(60))
                    .filter(content -> !content.isEmpty() && !content.equals("[DONE]"))
                    .map(content -> {
                        try {
                            // 解析JSON并提取文本内容
                            JsonNode jsonNode = objectMapper.readTree(content);
                            JsonNode choicesNode = jsonNode.get("choices");
                            if (choicesNode != null && choicesNode.size() > 0) {
                                JsonNode deltaNode = choicesNode.get(0).get("delta");
                                if (deltaNode != null && deltaNode.has("content")) {
                                    return deltaNode.get("content").asText();
                                }
                            }
                            return "";
                        } catch (Exception e) {
                            log.warn("解析流式响应失败，返回原始内容: {}", e.getMessage());
                            return content;
                        }
                    });
        } catch (Exception e) {
            log.error("初始化智谱AI流式服务失败", e);
            return Flux.error(new RuntimeException("初始化AI服务失败: " + e.getMessage()));
        }
    }

    @Override
    public String chat(String message) {
        // 获取智谱AI配置
        AIConfigurationDTO config = configurationService.getConfigurationByProvider("zhipuai");
        if (config == null || !StringUtils.hasText(config.getApiKey())) {
            throw new RuntimeException("智谱AI配置未找到或API Key未配置");
        }

        String baseUrl = StringUtils.hasText(config.getBaseUrl()) ? config.getBaseUrl() : "https://open.bigmodel.cn/api/paas/v4";
        String model = StringUtils.hasText(config.getModel()) ? config.getModel() : "glm-4";

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 65535);

        // 构建消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        requestBody.put("messages", new Object[]{userMessage});

        // 创建WebClient
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

        log.info("开始调用智谱AI LangChain 非流式聊天，模型: {}", model);

        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }

    // 难题：让 MCP Tools 流程用 Flux 优化输出效率
    @Override
    public Flux<String> streamChat(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        try {
            // 获取智谱AI配置
            AIConfigurationDTO config = configurationService.getConfigurationByProvider("zhipuai");
            if (config == null || !StringUtils.hasText(config.getApiKey())) {
                return Flux.error(new RuntimeException("智谱AI配置未找到或API Key未配置"));
            }

            String baseUrl = StringUtils.hasText(config.getBaseUrl()) ? config.getBaseUrl() : "https://open.bigmodel.cn/api/paas/v4";
            String model = StringUtils.hasText(config.getModel()) ? config.getModel() : "glm-4";

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);
            requestBody.put("tools", tools);
            requestBody.put("messages", messages);

            // 创建WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            log.info("开始调用智谱AI LangChain 流式聊天，模型: {}", model);

            // 4. 创建上下文对象，用于跨流片段累积状态
            StreamContext context = new StreamContext();
            AtomicReference<StreamContext> contextRef = new AtomicReference<>(context);

//            log.info("Requesting LLM with messages size: {}", messages.size());

            // 5. 发起请求并处理流
            log.info("Requesting LLM with messages body: {}", JSON.toJSONString(requestBody));
            Flux<String> currentStream = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(Duration.ofSeconds(60))
                    // 过滤掉非数据行
                    .filter(line -> !line.trim().isEmpty() && !line.equals("[DONE]"))
                    .map(jsonStr -> {
                        try {
                            // 解析每一帧 JSON
                            JsonNode root = objectMapper.readTree(jsonStr);
                            JsonNode choices = root.get("choices");
                            if (choices == null || choices.isEmpty()) return "";

                            JsonNode delta = choices.get(0).get("delta");

                            // A. 处理普通文本内容
                            if (delta.has("content") && !delta.get("content").isNull()) {
                                String text = delta.get("content").asText();
                                contextRef.get().fullContent.append(text); // 记录完整回复用于历史
                                return text; // 直接返回给前端显示
                            }

                            // B. 处理工具调用 (Tool Calls)
                            // 注意：GLM/OpenAI 的工具参数是流式传输的，需要拼接
                            if (delta.has("tool_calls")) {
                                contextRef.get().isToolCall = true;
                                JsonNode toolCall = delta.get("tool_calls").get(0); // 通常一次只流式传输一个工具

                                if (toolCall.has("id")) {
                                    contextRef.get().toolId = toolCall.get("id").asText();
                                }
                                if (toolCall.has("function")) {
                                    JsonNode func = toolCall.get("function");
                                    if (func.has("name")) {
                                        contextRef.get().toolName = func.get("name").asText();
                                    }
                                    if (func.has("arguments")) {
                                        contextRef.get().toolArgsBuffer.append(func.get("arguments").asText());
                                    }
                                }
                            }
                            return ""; // 工具调用片段不作为文本输出给前端（或者你可以输出一些 loading 提示）
                        } catch (Exception e) {
                            log.error("Error parsing stream chunk", e);
                            return "";
                        }
                    });

            // 6. 关键优化：使用 concatWith 处理流结束后的逻辑 (工具执行 + 递归)
            return currentStream.concatWith(Flux.defer(() -> {
                StreamContext ctx = contextRef.get();

                // 如果这一轮流结束了，并且检测到了完整的工具调用
                if (ctx.hasCompleteToolCall()) {
                    log.info("Tool Call Detected: {} with args {}", ctx.toolName, ctx.toolArgsBuffer);

                    // 6.1 执行工具 (这里是模拟，你需要调用你的 MCP Client)
                    String toolResult = mcpClient.executeMcpTool(ctx.toolName, ctx.toolArgsBuffer.toString());

                    // 6.2 更新对话历史
                    // (1) 添加 Assistant 的 Tool Call 消息
                    List<Map<String, Object>> nextMessages = new ArrayList<>(messages);

                    Map<String, Object> assistantMsg = new HashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", null);

                    // 构造标准的 tool_calls 结构
                    Map<String, Object> toolCallObj = new HashMap<>();
                    toolCallObj.put("id", ctx.toolId != null ? ctx.toolId : "call_" + UUID.randomUUID());
                    toolCallObj.put("type", "function");
                    toolCallObj.put("function", Map.of(
                            "name", ctx.toolName,
                            "arguments", ctx.toolArgsBuffer.toString()
                    ));
                    assistantMsg.put("tool_calls", List.of(toolCallObj));
                    nextMessages.add(assistantMsg);

                    // (2) 添加 Tool 的执行结果消息
                    Map<String, Object> toolMsg = new HashMap<>();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", toolCallObj.get("id"));
                    toolMsg.put("content", toolResult);
                    nextMessages.add(toolMsg);

                    // 6.3 递归调用！将工具结果发回给 LLM，获取最终解释
                    return streamChat(nextMessages, tools);
                }

                // 如果没有工具调用，直接结束
                return Flux.empty();
            }));
        } catch (Exception e) {
            log.error("初始化智谱AI流式服务失败", e);
            return Flux.error(new RuntimeException("初始化AI服务失败: " + e.getMessage()));
        }
    }

    // 定义一个简单的内部类来持有流的状态
    @Data
    private static class StreamContext {
        StringBuilder fullContent = new StringBuilder(); // 累积普通文本
        StringBuilder toolArgsBuffer = new StringBuilder(); // 累积工具参数
        String toolName = null;
        String toolId = null;
        boolean isToolCall = false;

        // 标记是否已经完成了工具调用的积攒
        public boolean hasCompleteToolCall() {
            return isToolCall && toolName != null && toolArgsBuffer.length() > 0;
        }
    }
}