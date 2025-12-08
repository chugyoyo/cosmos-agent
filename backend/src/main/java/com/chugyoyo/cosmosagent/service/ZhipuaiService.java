package com.chugyoyo.cosmosagent.service;

import com.chugyoyo.cosmosagent.dto.ChatMessageDTO;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;


public interface ZhipuaiService {

    Flux<String> streamChat(String message);

    String chat(String message);

    Flux<String> streamChat(List<Map<String, Object>> messages, List<Map<String, Object>> tools);
}