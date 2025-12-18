//package com.chugyoyo.cosmosagent.mcp.server;
//
//import com.chugyoyo.cosmosagent.mcp.model.ToolDefinition;
//import com.chugyoyo.cosmosagent.mcp.model.ToolRegistry;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationListener;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//import java.util.Map;
//
//@Component
//@RequiredArgsConstructor
//public class ToolAnnotationProcessor implements ApplicationListener<ApplicationReadyEvent> {
//
//    private final ApplicationContext applicationContext;
//    private final ToolRegistry toolRegistry;
//
//    @Override
//    public void onApplicationEvent(ApplicationReadyEvent event) {
//        String[] beanNames = applicationContext.getBeanDefinitionNames();
//        for (String beanName : beanNames) {
//            Object bean = applicationContext.getBean(beanName);
//            // 扫描类中的所有方法
//            for (Method method : bean.getClass().getDeclaredMethods()) {
//                if (method.isAnnotationPresent(AiTool.class)) {
//                    AiTool annotation = method.getAnnotation(AiTool.class);
//                    String toolName = annotation.name().isEmpty() ? method.getName() : annotation.name();
//
//                    // 1. 构建工具定义 (Schema 生成逻辑这里简化了，实际需要用 Jackson 生成 JSON Schema)
//                    ToolDefinition def = ToolDefinition.builder()
//                            .name(toolName)
//                            .description(annotation.description())
//                            .jsonSchema("{\"type\": \"object\", ...}") // 实际项目中需动态生成参数 Schema
//                            .build();
//
//                    // 2. 注册执行器
//                    toolRegistry.register(toolName, def, args -> {
//                        // 简单的参数映射逻辑，实际需要更复杂的反射处理
//                        Object[] methodArgs = mapArgsToMethod(method, args);
//                        return String.valueOf(method.invoke(bean, methodArgs));
//                    });
//                }
//            }
//        }
//    }
//
//    // 辅助方法：将 Map 参数转为方法参数 (伪代码)
//    private Object[] mapArgsToMethod(Method method, Map<String, Object> args) {
//        // 实现参数绑定逻辑...
//        return new Object[]{args.get("city")};
//    }
//}
