package com.chugyoyo.cosmosagent.workflow.llm;

import com.chugyoyo.cosmosagent.service.ZhipuaiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZhipuaiStrategy implements LLMStrategy {
    
    private final ZhipuaiService zhipuaiService;
    
    @Override
    public LLMResponse execute(String prompt, Map<String, Object> parameters) {
        try {
            log.info("Executing Zhipuai strategy with prompt: {}", prompt);
            
            // 调用智谱AI服务
            Flux<String> streamedChat = zhipuaiService.streamChat(prompt);
            StringBuilder responseBuilder = new StringBuilder();
            streamedChat.collectList().block().forEach(responseBuilder::append);
            String response = responseBuilder.toString();
            
            log.info("Zhipuai response received: {}", response);
            return LLMResponse.success(response, Map.of(
                "provider", "zhipuai",
                "model", parameters.getOrDefault("model", "glm-4"),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Zhipuai execution failed", e);
            return LLMResponse.failure("智谱AI调用失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getStrategyName() {
        return "zhipuai";
    }
    
    @Override
    public boolean validateParameters(Map<String, Object> parameters) {
        return parameters != null;
    }
}