package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
}