package com.chugyoyo.cosmosagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.chugyoyo.cosmosagent.mapper")
public class CosmosAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CosmosAgentApplication.class, args);
    }

}
