package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SpringAiTestService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Lazy
    private AIConfigurationService configurationService;

    public Map<String, Object> testConnection(String provider) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始测试 AI 连接，提供商: {}", provider);
            
            // 获取配置
            AIConfigurationDTO config = configurationService.getConfigurationByProvider(provider);
            if (config == null) {
                result.put("success", false);
                result.put("message", "未找到 " + provider + " 的配置");
                return result;
            }

            // 验证基本配置
            if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "API Key 未配置");
                return result;
            }

            // 根据不同的提供商进行测试
            switch (provider.toLowerCase()) {
                case "zhipuai":
                    result = testZhipuAIConnection(config);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "不支持的 AI 提供商: " + provider);
            }
            
            log.info("AI 连接测试完成，提供商: {}, 结果: {}", provider, result.get("success"));
            
        } catch (Exception e) {
            log.error("测试 AI 连接时发生错误", e);
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
        }
        
        return result;
    }

    private Map<String, Object> testZhipuAIConnection(AIConfigurationDTO config) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试智谱AI连接 - 简单的API调用测试
            String testUrl = (StringUtils.hasText(config.getBaseUrl()) ? config.getBaseUrl() : "https://open.bigmodel.cn/api/paas/v4") + "/chat/completions";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", StringUtils.hasText(config.getModel()) ? config.getModel() : "glm-4");
            
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "请回复'连接测试成功'来验证AI连接正常工作。");
            
            requestBody.put("messages", new Object[]{message});
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 50);
            
            // 设置请求头
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + config.getApiKey());
            headers.set("Content-Type", "application/json");
            
            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<Map> response = restTemplate.postForEntity(testUrl, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("choices")) {
                    result.put("success", true);
                    result.put("message", "智谱AI 连接测试成功");
                    result.put("provider", "智谱AI");
                    result.put("model", config.getModel());
                } else {
                    result.put("success", false);
                    result.put("message", "智谱AI 响应格式异常");
                }
            } else {
                result.put("success", false);
                result.put("message", "智谱AI 连接失败，状态码: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("智谱AI 连接测试失败", e);
            result.put("success", false);
            result.put("message", "智谱AI 连接失败: " + e.getMessage());
        }
        
        return result;
    }
}