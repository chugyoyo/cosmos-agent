package com.chugyoyo.cosmosagent.service;

import reactor.core.publisher.Flux;


public interface ZhipuaiService {

    Flux<String> streamChat(String message);
}