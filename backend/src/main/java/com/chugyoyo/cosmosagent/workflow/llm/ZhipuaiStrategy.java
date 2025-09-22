package com.chugyoyo.cosmosagent.workflow.llm;

import com.alibaba.fastjson2.JSON;
import com.chugyoyo.cosmosagent.service.ZhipuaiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
            String response = zhipuaiService.chat(prompt);
            log.info("Zhipuai response received: {}", response);

            ZhipuChatRespDto chatRespDto = JSON.parseObject(response, ZhipuChatRespDto.class);
            String content = "未找到内容";
            if (chatRespDto != null && !CollectionUtils.isEmpty(chatRespDto.getChoices())) {
                ZhipuChatRespDto.Choice firstChoice = chatRespDto.getChoices().get(0);
                if (firstChoice != null && firstChoice.getMessage() != null) {
                    content = firstChoice.getMessage().getContent();
                }
            }

            return LLMResponse.success(content);
            
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
        if (parameters == null) {
            return false;
        }
        
        // 检查outputVariable参数（可选）
        if (parameters.containsKey("outputVariable")) {
            Object outputVar = parameters.get("outputVariable");
            if (!(outputVar instanceof String) || ((String) outputVar).trim().isEmpty()) {
                log.warn("outputVariable parameter should be a non-empty string");
                return false;
            }
        }
        
        return true;
    }
}