package com.chugyoyo.cosmosagent.workflow;

import com.chugyoyo.cosmosagent.dto.WorkflowExecutionRequest;
import com.chugyoyo.cosmosagent.dto.WorkflowExecutionResponse;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class WorkflowContext {

    private final String executionId;
    private final WorkflowExecutionRequest request;
    private final LocalDateTime startTime;
    private WorkflowState state;
    private String errorMessage;

    @JsonIgnore
    private AgentNode currentNode;
    @JsonIgnore
    private WorkflowExecutionResponse.ExecutionStep lastStep;

    public WorkflowContext(String executionId, WorkflowExecutionRequest request) {
        this.executionId = executionId;
        this.request = request;
        this.startTime = LocalDateTime.now();
        this.state = WorkflowState.INITIALIZING;
    }
}
