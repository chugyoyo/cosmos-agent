package com.chugyoyo.cosmosagent.handler;

import com.chugyoyo.cosmosagent.common.ApiResp;
import com.chugyoyo.cosmosagent.common.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResp<String> handleException(Exception e) {
        log.error("Global Exception: {}", e.getMessage(), e);
        return ApiResp.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}
