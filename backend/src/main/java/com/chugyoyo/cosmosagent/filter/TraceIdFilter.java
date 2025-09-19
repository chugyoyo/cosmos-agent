package com.chugyoyo.cosmosagent.filter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = request.getHeader(TRACE_ID);
            if (StringUtils.isBlank(traceId)) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
            MDC.put(TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
