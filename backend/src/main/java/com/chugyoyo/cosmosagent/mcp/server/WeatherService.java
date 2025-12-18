package com.chugyoyo.cosmosagent.mcp.server;

import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @AiTool(description = "Get the current weather for a given location. Args: city (string)")
    public String getWeather(String city) {
        // 模拟 API 调用
        if (city.contains("GuangZhou")) return "Sunny, 25°C";
        return "Cloudy, 18°C";
    }
}