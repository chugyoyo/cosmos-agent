package com.chugyoyo.cosmosagent.common;

import com.chugyoyo.cosmosagent.filter.TraceIdFilter;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResp<T> {
    
    private Integer code;
    private String message;
    private T data;
    private String traceId;
    
    public static <T> ApiResp<T> success(T data) {
        return new ApiResp<>(HttpStatus.OK.getCode(), HttpStatus.OK.getMessage(), data, MDC.get(TraceIdFilter.TRACE_ID));
    }

    public static <T> ApiResp<T> success() {
        return new ApiResp<>(HttpStatus.OK.getCode(), HttpStatus.OK.getMessage(), null, MDC.get(TraceIdFilter.TRACE_ID));
    }

    public static <T> ApiResp<T> fail(HttpStatus httpStatus, String message) {
        return new ApiResp<>(httpStatus.getCode(), message, null, MDC.get(TraceIdFilter.TRACE_ID));
    }

    public static <T> ApiResp<T> fail(String message) {
        return new ApiResp<>(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), message, null, MDC.get(TraceIdFilter.TRACE_ID));
    }

}
