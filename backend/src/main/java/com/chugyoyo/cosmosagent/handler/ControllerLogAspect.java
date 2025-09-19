package com.chugyoyo.cosmosagent.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class ControllerLogAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 切所有 @RestController 或 @Controller 下的方法
    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        // 获取方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // 构造日志信息
        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("httpMethod", request.getMethod());
        logInfo.put("requestURI", request.getRequestURI());
        logInfo.put("javaMethod", method.getDeclaringClass().getName() + "." + method.getName());

        // 获取请求参数 - 跳过MultipartFile以避免序列化错误
        Object[] args = joinPoint.getArgs();
        Object[] safeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) args[i];
                safeArgs[i] = "MultipartFile{name=" + file.getOriginalFilename() +
                        ", size=" + file.getSize() + ", contentType=" + file.getContentType() + "}";
            } else {
                safeArgs[i] = args[i];
            }
        }
        logInfo.put("requestParams", safeArgs);

        // 打印请求日志
        try {
            log.info("receive request: {}", objectMapper.writeValueAsString(logInfo));
        } catch (Exception e) {
            log.warn("Failed to serialize request log: {}", e.getMessage());
            log.info("Controller Request: httpMethod={}, requestURI={}, javaMethod={}", request.getMethod(), request.getRequestURI(), method.getDeclaringClass().getName() + "." + method.getName());
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        return result;
    }
}

