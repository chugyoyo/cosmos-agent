package com.chugyoyo.cosmosagent.mcp.server;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.*;
import java.util.stream.Collectors;

@Slf4j
public class FileSystemTools {

    // 建议限制 Agent 只能操作项目路径下的文件
    private static final String BASE_PATH = System.getProperty("user.dir");

    @Tool(name = "read_file", description = "Read the content of a file (e.g., source code, README, logs).")
    public String readFile(
            @ToolParam(name = "filePath", description = "Relative path to the file from project root") String filePath) {
        try {
            Path path = Paths.get(BASE_PATH, filePath);
            if (!Files.exists(path)) return "Error: File not found at " + filePath;
            return Files.readString(path);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(name = "write_file", description = "Overwrite or create a file with new content. Use this to update documentation or rewrite prompts.")
    public String writeFile(
            @ToolParam(name = "filePath", description = "Target file path") String filePath,
            @ToolParam(name = "content", description = "Full text content to write") String content) {
        try {
            Path path = Paths.get(BASE_PATH, filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "File updated successfully: " + filePath;
        } catch (Exception e) {
            return "Error writing file: " + e.getMessage();
        }
    }

    @Tool(name = "search_in_files", description = "Search for a keyword or regex in the project files (like grep).")
    public String searchFiles(
            @ToolParam(name = "keyword", description = "Keyword to search for") String keyword) {
        try {
            // 简单的 Java 实现 grep 逻辑
            return Files.walk(Paths.get(BASE_PATH))
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.toString().contains(".git") && !p.toString().contains("target"))
                    .filter(p -> {
                        try { return Files.readString(p).contains(keyword); }
                        catch (Exception e) { return false; }
                    })
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Error searching files: " + e.getMessage();
        }
    }
}