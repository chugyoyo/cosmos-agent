package com.chugyoyo.cosmosagent.workflow.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LLMStrategyFactory {
    
    private final Map<String, LLMStrategy> strategies;

    @Autowired
    public LLMStrategyFactory(List<LLMStrategy> strategyList) {
        this.strategies = new ConcurrentHashMap<>();
        for (LLMStrategy strategy : strategyList) {
            strategies.put(strategy.getStrategyName(), strategy);
        }
        log.info("Initialized LLM strategies: {}", strategies.keySet());
    }
    
    /**
     * 获取LLM策略
     * @param strategyName 策略名称
     * @return LLM策略实例
     */
    public LLMStrategy getStrategy(String strategyName) {
        LLMStrategy strategy = strategies.get(strategyName.toLowerCase());
        if (strategy == null) {
            log.warn("Strategy not found: {}, using default zhipuai strategy", strategyName);
            strategy = strategies.get("zhipuai");
        }
        return strategy;
    }
    
    /**
     * 获取所有可用策略
     */
    public Map<String, LLMStrategy> getAllStrategies() {
        return Map.copyOf(strategies);
    }
}