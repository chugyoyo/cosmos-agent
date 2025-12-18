package com.chugyoyo.cosmosagent.mcp.server;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;

// Tool class
@Slf4j
public class SimpleTools {
    @Tool(name = "get_time", description = "Get current time")
    public String getTime(
            @ToolParam(name = "zone", description = "Timezone, e.g., Beijing") String zone) {
        log.info("Get time in zone: {}", zone);
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
