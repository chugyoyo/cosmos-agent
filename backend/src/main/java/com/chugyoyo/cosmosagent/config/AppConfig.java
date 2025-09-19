package com.chugyoyo.cosmosagent.config;

import com.chugyoyo.cosmosagent.interceptor.LoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 添加拦截器到 RestTemplate
        restTemplate.getInterceptors().add(new LoggingInterceptor());
        return restTemplate;
    }
}