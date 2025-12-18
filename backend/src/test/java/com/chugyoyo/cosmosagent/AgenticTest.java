package com.chugyoyo.cosmosagent;

import com.chugyoyo.cosmosagent.common.LlmProvierEnum;
import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.mcp.server.ApiInvokerTools;
import com.chugyoyo.cosmosagent.mcp.server.ClassInspectorTools;
import com.chugyoyo.cosmosagent.mcp.server.DatabaseTools;
import com.chugyoyo.cosmosagent.mcp.server.ProjectDocTools;
import com.chugyoyo.cosmosagent.mcp.server.ReportingTools;
import com.chugyoyo.cosmosagent.service.AIConfigurationService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@SpringBootTest
public class AgenticTest {

    @Autowired
    private AIConfigurationService aiConfigurationService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void test() {
        // get configuration by provider
        AIConfigurationDTO configuration = aiConfigurationService.getConfigurationByProvider(LlmProvierEnum.DASHSCOPE);

        // Prepare tools
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ProjectDocTools(applicationContext));
        toolkit.registerTool(new ClassInspectorTools());
        toolkit.registerTool(new ReportingTools());
        toolkit.registerTool(new ApiInvokerTools(restTemplate));
        DatabaseTools.setDataSource(dataSource);
        toolkit.registerTool(new DatabaseTools());

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有 10 年互联网大厂经验的测试专家，请根据提供的 API 文档，回答用户的问题")
                .model(DashScopeChatModel.builder()
                        .apiKey(configuration.getApiKey())
                        .modelName("qwen-max")
                        .build())
                .toolkit(toolkit)
                .build();

        String content = """
                请对项目里的所有接口进行一轮流程测试，然后将测试报告写到本地文件中，必要时可以调用工具。
                注意：
                1. 如果错误的话，可以找原因然后想方法去执行，必要时可以查询 SQL 去数据库里找数据
                2. 如果失败超过 10 次，这个接口将不再进行测试，标记最后一次失败的原因
                3. 请保证所有接口都被测试过
                4. 面对 /mcp/sse 接口不测试
                5. 像 ../{id}/.. 这种路径参数，直接替换为测试的变量，然后进行请求
                """;
        Msg response = agent.call(Msg.builder()
                .textContent(content)
                .build()).block();
        System.out.println(response.getTextContent());
    }
}
