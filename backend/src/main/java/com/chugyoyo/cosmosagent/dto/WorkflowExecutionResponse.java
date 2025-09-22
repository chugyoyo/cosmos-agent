package com.chugyoyo.cosmosagent.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowExecutionResponse {
    
    private String executionId;
    private Long agentId;
    private String status;
    private String currentStep;
    private List<ExecutionStep> steps;
    private Map<String, Object> result;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionStep {
        private String stepId;
        private String nodeName;
        private String nodeType;
        private String status;
        private Map<String, Object> input;
        private Map<String, Object> output;
        private String errorMessage;
        private Long duration;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}