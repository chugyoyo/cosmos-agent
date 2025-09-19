package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZhipuaiServiceImpl implements ZhipuaiService {
    
    @Autowired
    @Lazy
    private AIConfigurationService configurationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Flux<String> chat(String message, String model) {
        return chatWithHistory(message, model, null);
    }
    
    @Override
    public Flux<String> chatWithHistory(String message, String model, String conversationHistory) {
        try {
            // 获取智谱AI配置
            AIConfigurationDTO config = configurationService.getConfigurationByProvider("zhipuai");
            if (config == null) {
                return Flux.error(new RuntimeException("未找到智谱AI配置"));
            }
            
            if (!StringUtils.hasText(config.getApiKey())) {
                return Flux.error(new RuntimeException("智谱AI API Key未配置"));
            }
            
            String baseUrl = StringUtils.hasText(config.getBaseUrl()) ? config.getBaseUrl() : "https://open.bigmodel.cn/api/paas/v4";
            String chatModel = StringUtils.hasText(model) ? model : (StringUtils.hasText(config.getModel()) ? config.getModel() : "glm-4");
            
            // 创建WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                    .defaultHeader("Content-Type", "application/json")
                    .build();
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", chatModel);
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);
            
            // 构建消息历史
            if (StringUtils.hasText(conversationHistory)) {
                // 如果有历史记录，解析并添加
                JsonNode historyNode = objectMapper.readTree(conversationHistory);
                requestBody.put("messages", historyNode);
            } else {
                // 没有历史记录，只添加当前消息
                Map<String, Object> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", message);
                requestBody.put("messages", new Object[]{userMessage});
            }
            
            log.info("开始调用智谱AI API，模型: {}", chatModel);
            
            // 发送流式请求
            return webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .map(this::extractContentFromSSE)
                    .onErrorResume(e -> {
                        log.error("智谱AI API调用失败", e);
                        return Flux.error(new RuntimeException("AI服务调用失败: " + e.getMessage()));
                    });
                    
        } catch (Exception e) {
            log.error("初始化智谱AI服务失败", e);
            return Flux.error(new RuntimeException("初始化AI服务失败: " + e.getMessage()));
        }
    }
    
    private String extractContentFromSSE(String sseLine) {
        try {
            if (!sseLine.startsWith("data: ")) {
                return "";
            }
            
            String jsonStr = sseLine.substring(6);
            if ("[DONE]".equals(jsonStr.trim())) {
                return "[DONE]";
            }
            
            JsonNode node = objectMapper.readTree(jsonStr);
            JsonNode choices = node.get("choices");
            if (choices != null && choices.size() > 0) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("content")) {
                    return delta.get("content").asText();
                }
            }
            
            return "";
        } catch (Exception e) {
            log.warn("解析SSE数据失败: {}", sseLine, e);
            return "";
        }
    }
}