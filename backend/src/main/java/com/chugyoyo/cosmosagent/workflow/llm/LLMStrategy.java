package com.chugyoyo.cosmosagent.workflow.llm;

import java.util.Map;

public interface LLMStrategy {
    
    /**
     * 执行LLM调用
     * @param prompt 提示词
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
    class LLMResponse {
        private final String content;
        private final Map<String, Object> metadata;
        private final boolean success;
        private final String errorMessage;
        
        public LLMResponse(String content, Map<String, Object> metadata, boolean success, String errorMessage) {
            this.content = content;
            this.metadata = metadata;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public static LLMResponse success(String content, Map<String, Object> metadata) {
            return new LLMResponse(content, metadata, true, null);
        }
        
        public static LLMResponse failure(String errorMessage) {
            return new LLMResponse(null, null, false, errorMessage);
        }
        
        // Getters
        public String getContent() { return content; }
        public Map<String, Object> getMetadata() { return metadata; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
}