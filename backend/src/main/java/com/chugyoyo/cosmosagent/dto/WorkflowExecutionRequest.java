package com.chugyoyo.cosmosagent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecutionRequest {
    
    @NotNull(message = "代理ID不能为空")
    private Long agentId;
    
    private Map<String, Object> params;
}