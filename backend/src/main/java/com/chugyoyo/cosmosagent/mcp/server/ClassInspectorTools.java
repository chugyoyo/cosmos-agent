package com.chugyoyo.cosmosagent.mcp.server;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class ClassInspectorTools {

    /**
     * 工具 3: 查看 DTO/Entity 内部字段详情
     * 当 Agent 从 API 文档中发现复杂类型时，调用此工具查看结构
     */
    @Tool(name = "get_class_structure",
            description = "Inspect the fields of a specific Java class (DTO/Entity). " +
                    "Provide the simple name or full name of the class to see its attributes and types.")
    public String getClassStructure(
            @ToolParam(name = "className", description = "The name of the class to inspect, e.g., 'UserDTO'") String className) {

        try {
            // 提示：在实际 Spring 项目中，可能需要根据类名从 ApplicationContext 或 ClassLoader 中查找
            // 这里演示通过反射获取类。如果 Agent 只给简写名，你可能需要一个扫描后的 Map 来映射
            Class<?> clazz = findClass(className);
            if (clazz == null) {
                return "Error: Could not find class " + className + ". Please provide the full package name if possible.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Structure of ").append(clazz.getSimpleName()).append(":\n");

            // 获取所有字段，包括私有的
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // 过滤掉合成字段（如覆盖率工具产生的字段）和静态常量
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String fieldName = field.getName();
                String fieldType = field.getType().getSimpleName();

                // 处理泛型（如 List<OrderDTO>）
                if (Collection.class.isAssignableFrom(field.getType())) {
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        Type[] actualArgs = ((ParameterizedType) genericType).getActualTypeArguments();
                        if (actualArgs.length > 0) {
                            fieldType += "<" + getSimpleTypeName(actualArgs[0].getTypeName()) + ">";
                        }
                    }
                }

                sb.append(String.format("  - %s: %s\n", fieldName, fieldType));
            }

            return sb.toString();

        } catch (Exception e) {
            log.error("Failed to inspect class: {}", className, e);
            return "Error inspecting class: " + e.getMessage();
        }
    }

    // 辅助方法：通过简单的类名匹配已加载的类（实际项目中建议预先扫描所有 DTO 建立索引）
    private Class<?> findClass(String className) {
        // 尝试直接加载
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
        }

        // 实际开发中，你可以遍历常用 package (如 com.yourproject.dto.*) 来匹配
        // 这里仅作演示逻辑
        return null;
    }

    private String getSimpleTypeName(String fullTypeName) {
        int lastDot = fullTypeName.lastIndexOf('.');
        return lastDot > 0 ? fullTypeName.substring(lastDot + 1) : fullTypeName;
    }
}