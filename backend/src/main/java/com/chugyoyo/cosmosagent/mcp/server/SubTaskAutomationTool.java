package com.chugyoyo.cosmosagent.mcp.server;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubTaskAutomationTool {

    private final String apiKey;
    private final Toolkit globalToolkit; // 所有的原子工具池（SQL, API, etc.）

    public SubTaskAutomationTool(String apiKey, Toolkit globalToolkit) {
        this.apiKey = apiKey;
        this.globalToolkit = globalToolkit;
    }

    @Tool(name = "execute_subtask",
            description = "Delegate a specific sub-task to a specialized sub-agent. " +
                    "Use this to handle complex steps in parallel or break down a large goal.")
    public String executeSubtask(
            @ToolParam(name = "taskName", description = "Brief name of the sub-task") String taskName,
            @ToolParam(name = "instruction", description = "Detailed instructions for the sub-agent") String instruction,
            @ToolParam(name = "contextData", description = "Specific data needed (e.g., API path, table name)") String contextData) {

        log.info("--- Starting Sub-Agent: [{}] ---", taskName);

        try {
            // 1. 为子智能体准备独立的运行环境
            // 这里可以直接复用 globalToolkit，或者根据指令过滤一部分工具
            ReActAgent subAgent = ReActAgent.builder()
                    .name(taskName)
                    .sysPrompt(String.format(
                            "你是一个专项任务执行者。任务名称：%s。你的目标是根据提供的上下文和指令，" +
                                    "利用工具独立完成任务并给出最终结果。不要废话，直接执行。", taskName))
                    .model(DashScopeChatModel.builder()
                            .apiKey(apiKey)
                            .modelName("qwen-max")
                            .build())
                    .toolkit(globalToolkit) // 子智能体拥有操作原子工具的能力
                    .build();

            // 2. 构造任务指令
            String prompt = String.format("任务指令: %s\n上下文数据: %s", instruction, contextData);

            // 3. 同步调用子智能体并等待 block
            Msg response = subAgent.call(Msg.builder().textContent(prompt).build()).block();

            String result = response.getTextContent();
            log.info("--- Sub-Agent [{}] Finished ---", taskName);

            // 返回给主 Agent 的结果，主 Agent 会根据这个结果决定下一步
            return String.format("Sub-task [%s] execution result: %s", taskName, result);

        } catch (Exception e) {
            log.error("Sub-task execution failed", e);
            return "Error executing sub-task: " + e.getMessage();
        }
    }
}
