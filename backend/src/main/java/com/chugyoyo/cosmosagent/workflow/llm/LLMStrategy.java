package com.chugyoyo.cosmosagent.workflow.llm;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

public interface LLMStrategy {

    /**
     * 执行LLM调用
     *
     * @param prompt     提示词
     * @param parameters 参数
     * @return LLM响应结果
     */
    LLMResponse execute(String prompt, Map<String, Object> parameters);

    /**
     * 获取策略名称
     */
    String getStrategyName();

    /**
     * 验证参数
     */
    boolean validateParameters(Map<String, Object> parameters);

    /**
     * LLM响应结果
     */
    @Getter
    @AllArgsConstructor
    public static class LLMResponse {
        // Getters
        private final String content;
        private final boolean success;
        private final String errorMessage;

        public static LLMResponse success(String content) {
            return new LLMResponse(content, true, null);
        }

        public static LLMResponse failure(String errorMessage) {
            return new LLMResponse(errorMessage, false, errorMessage);
        }

    }
}