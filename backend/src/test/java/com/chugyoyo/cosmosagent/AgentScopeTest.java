package com.chugyoyo.cosmosagent;

import com.chugyoyo.cosmosagent.common.LlmProvierEnum;
import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.mcp.server.SimpleTools;
import com.chugyoyo.cosmosagent.service.AIConfigurationService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CosmosAgentApplication.class)
public class AgentScopeTest {

    @Autowired
    private AIConfigurationService aiConfigurationService;

    @Test
    public void test() {

        // get configuration by provider
        AIConfigurationDTO configuration = aiConfigurationService.getConfigurationByProvider(LlmProvierEnum.DASHSCOPE);

        // Prepare tools
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new SimpleTools());


        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("You are a helpful AI assistant.")
                .model(DashScopeChatModel.builder()
                        .apiKey(configuration.getApiKey())
                        .modelName("qwen-max")
                        .build())
                .toolkit(toolkit)
                .build();

        Msg response = agent.call(Msg.builder()
                .textContent("Hello, what time is it now? My Timezone is Beijing.")
                .build()).block();
        System.out.println(response.getTextContent());
    }
}
