package com.chugyoyo.cosmosagent.service;

import reactor.core.publisher.Flux;

public interface ZhipuaiService {
    
    Flux<String> chat(String message, String model);
    
    Flux<String> chatWithHistory(String message, String model, String conversationHistory);
}