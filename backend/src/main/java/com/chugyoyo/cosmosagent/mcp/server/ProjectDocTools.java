package com.chugyoyo.cosmosagent.mcp.server;

import io.agentscope.core.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ProjectDocTools {

    private final ApplicationContext applicationContext;

    public ProjectDocTools(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Tool(name = "get_project_api_docs",
            description = "Scan the project's Spring Controllers and return a list of API endpoints, " +
                    "including URL, HTTP Method, Parameters, and Return Type. Use this to understand available APIs.")
    public String getProjectApiDocs() {
        try {
            // 从 Spring 容器中获取路由映射处理器
            RequestMappingHandlerMapping mapping = (RequestMappingHandlerMapping)
                    applicationContext.getBean("requestMappingHandlerMapping");
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

            List<Map<String, Object>> apiList = new ArrayList<>();

            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo info = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();

                Map<String, Object> apiInfo = new LinkedHashMap<>();

                // 1. 获取 URL 路径
                Set<String> urls = new HashSet<>();
                if (info.getPatternsCondition() != null) {
                    // 旧版 AntPathMatcher 策略
                    urls = info.getPatternsCondition().getPatterns();
                } else if (info.getPathPatternsCondition() != null) {
                    // 新版 PathPatternParser 策略
                    urls = info.getPathPatternsCondition().getPatternValues();
                }
                apiInfo.put("urls", urls);

                // 2. 获取 HTTP 方法 (GET, POST 等)
                Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
                apiInfo.put("methods", methods.isEmpty() ? "ALL" : methods);

                // 3. 获取方法签名信息
                Method method = handlerMethod.getMethod();
                apiInfo.put("controller", handlerMethod.getBeanType().getSimpleName());
                apiInfo.put("methodName", method.getName());

                // 4. 获取入参信息
                List<String> params = Arrays.stream(method.getParameters())
                        .map(p -> p.getType().getSimpleName() + " " + p.getName())
                        .collect(Collectors.toList());
                apiInfo.put("parameters", params);

                // 5. 获取出参类型
                apiInfo.put("returnType", method.getReturnType().getSimpleName());

                apiList.add(apiInfo);
            }

            // 转换成格式化的字符串/JSON 返回给 Agent
            StringBuilder sb = new StringBuilder("### Project API Documentation ###\n");
            for (Map<String, Object> api : apiList) {
                sb.append(String.format("- [%s] %s\n", api.get("methods"), api.get("urls")));
                sb.append(String.format("  Controller: %s#%s\n", api.get("controller"), api.get("methodName")));
                sb.append(String.format("  Params: %s\n", api.get("parameters")));
                sb.append(String.format("  Returns: %s\n\n", api.get("returnType")));
            }

            log.info("get API DOC {}", sb.toString());
            return sb.toString();

        } catch (Exception e) {
            log.error("Failed to scan API docs", e);
            return "Error scanning project APIs: " + e.getMessage();
        }
    }
}