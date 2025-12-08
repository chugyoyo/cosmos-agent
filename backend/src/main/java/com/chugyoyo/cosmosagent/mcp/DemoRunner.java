package com.chugyoyo.cosmosagent.mcp;

import com.chugyoyo.cosmosagent.mcp.service.McpClientService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoRunner {

    @Bean
    CommandLineRunner run(McpClientService clientService) {
        return args -> {
            // 启动后自动运行一次客户端调用演示
            try {
                // 等待 Spring Boot Tomcat 完全启动
                Thread.sleep(2000);
                clientService.runDemo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
