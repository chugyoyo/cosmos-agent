package com.chugyoyo.cosmosagent.workflow;

import com.alibaba.fastjson2.JSON;
import com.chugyoyo.cosmosagent.dto.WorkflowExecutionResponse;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import com.chugyoyo.cosmosagent.workflow.llm.LLMStrategy;
import com.chugyoyo.cosmosagent.workflow.llm.LLMStrategyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * LLM节点执行器
 * 负责调用大语言模型并处理响应
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LLMNodeExecutor implements AgentNodeExecutor {

    private final ObjectMapper objectMapper;
    private final LLMStrategyFactory llmStrategyFactory;

    @Override
    public WorkflowExecutionResponse.ExecutionStep execute(WorkflowContext context) throws Exception {

        LocalDateTime startTime = LocalDateTime.now();

        AgentNode currentNode = context.getCurrentNode();

        log.info("Executing LLM node: {}", currentNode.getName());

        // 解析LLM配置
        Map<String, Object> llmConfig = parseLlmConfig(currentNode);

        // 构建提示词
        String prompt = buildPrompt(llmConfig, context.getLastStep().getOutput());

        // 准备LLM参数
        Map<String, Object> llmParameters = prepareLlmParameters(llmConfig);

        // 获取LLM策略
        String model = (String) llmConfig.getOrDefault("model", "glm-4");
        LLMStrategy strategy = llmStrategyFactory.getStrategy(model);

        if (strategy == null) {
            throw new RuntimeException("不支持的LLM模型: " + model);
        }

        // 验证参数
        if (!strategy.validateParameters(llmParameters)) {
            throw new RuntimeException("LLM参数验证失败");
        }

        // 执行LLM调用
        LLMStrategy.LLMResponse llmResponse = strategy.execute(prompt, llmParameters);

        if (!llmResponse.isSuccess()) {
            throw new RuntimeException("LLM调用失败: " + llmResponse.getErrorMessage());
        }

        // 构建输出结果
        Map<String, Object> output = buildOutput(currentNode.getLlmConfig(), llmResponse.getContent());

        LocalDateTime endTime = LocalDateTime.now();
        long costMills = Duration.between(startTime, endTime).toMillis();
        log.info("LLM node executed successfully: {}, execution time: {}ms", currentNode.getName(), costMills);

        return WorkflowExecutionResponse.ExecutionStep.builder()
                .input(context.getLastStep().getOutput())
                .output(output)
                .nodeName(currentNode.getName())
                .nodeType(NodeType.LLM.name())
                .status(WorkflowState.COMPLETED.name())
                .startTime(startTime)
                .endTime(endTime)
                .duration(costMills)
                .build();
    }

    private Map<String, Object> buildOutput(String llmConfig, String content) {
        // 从配置中获取输出变量名
        Map<String, Object> map = JSON.parseObject(llmConfig, Map.class);
        String outputVariable = (String) map.getOrDefault("outputVariable", "llm_output");
        return Map.of(outputVariable, content);
    }

    @Override
    public NodeType getSupportedNodeType() {
        return NodeType.LLM;
    }

    private Map<String, Object> parseLlmConfig(AgentNode node) throws Exception {
        Map<String, Object> config = objectMapper.readValue(node.getLlmConfig(), Map.class);

        // 设置默认值
        config.putIfAbsent("systemPrompt", "");
        config.putIfAbsent("userPrompt", "");
        config.putIfAbsent("temperature", 0.7);
        config.putIfAbsent("maxTokens", 1000);
        config.putIfAbsent("outputVariable", "llm_output");

        return config;
    }

    private String buildPrompt(Map<String, Object> llmConfig, Map<String, Object> input) {
        String systemPrompt = (String) llmConfig.get("systemPrompt");
        String userPrompt = (String) llmConfig.get("userPrompt");

        // 处理输入变量替换
        userPrompt = replaceVariables(userPrompt, input);

        // 构建完整提示词
        StringBuilder prompt = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            prompt.append(systemPrompt).append("\n\n");
        }
        prompt.append(userPrompt);

        return prompt.toString();
    }

    private String replaceVariables(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }

        String result = template;
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }

    private Map<String, Object> prepareLlmParameters(Map<String, Object> llmConfig) {
        Map<String, Object> parameters = new HashMap<>();

        // 添加outputVariable参数
        parameters.put("outputVariable", llmConfig.get("outputVariable"));

        // 添加其他LLM特定参数
        if (llmConfig.containsKey("temperature")) {
            parameters.put("temperature", llmConfig.get("temperature"));
        }

        if (llmConfig.containsKey("maxTokens")) {
            parameters.put("max_tokens", llmConfig.get("maxTokens"));
        }

        return parameters;
    }
}
