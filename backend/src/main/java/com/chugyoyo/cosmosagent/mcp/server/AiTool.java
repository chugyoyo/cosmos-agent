package com.chugyoyo.cosmosagent.mcp.server;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AiTool {
    String name() default "";
    String description(); // 必须提供描述，这是 ReAct 的核心
}