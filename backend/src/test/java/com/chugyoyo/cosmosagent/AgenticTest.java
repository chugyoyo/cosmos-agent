package com.chugyoyo.cosmosagent;

import com.chugyoyo.cosmosagent.common.LlmProvierEnum;
import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.mcp.server.ApiInvokerTools;
import com.chugyoyo.cosmosagent.mcp.server.ClassInspectorTools;
import com.chugyoyo.cosmosagent.mcp.server.DatabaseTools;
import com.chugyoyo.cosmosagent.mcp.server.FileSystemTools;
import com.chugyoyo.cosmosagent.mcp.server.ProjectDocTools;
import com.chugyoyo.cosmosagent.mcp.server.SubTaskAutomationTool;
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
        AIConfigurationDTO config = aiConfigurationService.getConfigurationByProvider(LlmProvierEnum.DASHSCOPE);

        // 1. 准备底层的原子工具
        Toolkit atomicToolkit = new Toolkit();
        atomicToolkit.registerTool(new ProjectDocTools(applicationContext));
        atomicToolkit.registerTool(new DatabaseTools());
        atomicToolkit.registerTool(new ApiInvokerTools(restTemplate));
        atomicToolkit.registerTool(new FileSystemTools());

        // 2. 注册“递归任务工具”
        Toolkit orchestratorToolkit = new Toolkit();
        // 这里把原子工具传进去，让子智能体也能用
        orchestratorToolkit.registerTool(new SubTaskAutomationTool(config.getApiKey(), atomicToolkit));
        // 主 Agent 也可以拥有发现 API 的能力，以便它进行规划
        orchestratorToolkit.registerTool(new ProjectDocTools(applicationContext));
        orchestratorToolkit.registerTool(new DatabaseTools());
        orchestratorToolkit.registerTool(new ApiInvokerTools(restTemplate));
        orchestratorToolkit.registerTool(new FileSystemTools());


        // 3. 初始化主智能体（模仿 Claude Code 模式）
        String prompt = """
                你是一个拥有 10 年互联网大厂测试经验的测试专家，需要完成给定的任务。
                """;
        ReActAgent leadAgent = ReActAgent.builder()
                .name("LeadAgent")
                .sysPrompt(prompt)
                .model(DashScopeChatModel.builder().apiKey(config.getApiKey()).modelName("qwen-max").build())
                .toolkit(orchestratorToolkit)
                .build();

        // 4. 下达宏观指令
        String userPrompt = """
                当前任务：请全面测试项目中的所有 /agent/、/chat/ 路径的接口
                拆分子任务 1: 查看是否有 API DOC 文件，如果有则跳过，否则新建文件 api-docs.md
                拆分子任务 2: 若文件 api-docs.md 为空，获取最新的 API DOC 内容，然后写入该文件
                拆分子任务 3: 读取 api-docs.md，生成测试计划，将测试计划写入 api-test-plan.md
                拆分子任务 4: 根据 api-test-plan.md 中的测试计划，执行测试用例，将测试结果写入 api-test-results.md
                
                注意：
                - 请一步一步地思考。
                - 子任务执行是调用 execute_subtask 工具，任务内容必须清晰、上下文完整。
                """;
        Msg msg = leadAgent.call(Msg.builder().textContent(userPrompt).build()).block();
        System.out.println(msg.getTextContent());
    }
}
