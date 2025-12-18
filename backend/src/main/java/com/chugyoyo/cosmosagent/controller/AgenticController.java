package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.ApiResp;
import com.chugyoyo.cosmosagent.common.LlmProvierEnum;
import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.mcp.server.DatabaseTools;
import com.chugyoyo.cosmosagent.service.AIConfigurationService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@Slf4j
@RestController
@RequestMapping("/agentic")
public class AgenticController {

    @Autowired
    private AIConfigurationService aiConfigurationService;

    @Autowired
    private DataSource dataSource;

    @GetMapping("/auto-select-from-db")
    public ApiResp<String> autoSelectFromDb(@RequestParam String question) {
        // get configuration by provider
        AIConfigurationDTO configuration = aiConfigurationService.getConfigurationByProvider(LlmProvierEnum.DASHSCOPE);

        // Prepare tools
        Toolkit toolkit = new Toolkit();
        DatabaseTools databaseTools = new DatabaseTools();
        DatabaseTools.setDataSource(dataSource);
        toolkit.registerTool(databaseTools);

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个资深 DBA，你需要根据用户的问题，使用数据库工具来查询数据库。")
                .model(DashScopeChatModel.builder()
                        .apiKey(configuration.getApiKey())
                        .modelName("qwen-max")
                        .build())
                .toolkit(toolkit)
                .build();

        Msg response = agent.call(Msg.builder()
                .textContent(question)
                .build()).block();
        log.info("Response: {}", response.getTextContent());
        return ApiResp.success(response.getTextContent());
    }
}
