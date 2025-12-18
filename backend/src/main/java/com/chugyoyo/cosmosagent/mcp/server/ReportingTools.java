package com.chugyoyo.cosmosagent.mcp.server;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ReportingTools {

    private static final String REPORT_DIR = "./agent_reports/";

    public ReportingTools() {
        // 初始化报告目录
        try {
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (IOException e) {
            log.error("Failed to create report directory", e);
        }
    }

    /**
     * 工具 4: 写入测试或执行报告
     * Agent 在完成一系列操作后，应总结结果并调用此工具
     */
    @Tool(name = "save_execution_report",
            description = "Save the task execution result or test report to a persistent file. " +
                    "Include task summary, SQL results, and API call status.")
    public String saveExecutionReport(
            @ToolParam(name = "title", description = "The title of the report, e.g., 'User Cleanup Task'") String title,
            @ToolParam(name = "content", description = "The detailed content of the report in Markdown format.") String content) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_%s.md", timestamp, title.replaceAll("[^a-zA-Z0-9]", "_"));
        Path filePath = Paths.get(REPORT_DIR, fileName);

        try {
            // 构建带元数据的报告内容
            String fullContent = String.format(
                    "# %s\n\n- **Execution Time**: %s\n- **Status**: Completed\n\n---\n\n%s",
                    title,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    content
            );

            Files.write(filePath, fullContent.getBytes(), StandardOpenOption.CREATE);

            log.info("Report saved successfully: {}", filePath.toAbsolutePath());
            return "Report successfully saved to: " + fileName;

        } catch (IOException e) {
            log.error("Failed to save report", e);
            return "Error saving report: " + e.getMessage();
        }
    }
}
