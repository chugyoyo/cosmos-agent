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
    
    @NotNull(message = "工作流节点不能为空")
    private List<WorkflowNodeDTO> nodes;
    
    @NotNull(message = "工作流连线不能为空")
    private List<WorkflowLinkDTO> links;
    
    private Map<String, Object> params;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowNodeDTO {
        private Long id;
        private String name;
        private String type;
        private Integer positionX;
        private Integer positionY;
        private String config;
        private String yamlConfig;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowLinkDTO {
        private Long id;
        private Long sourceNodeId;
        private Long targetNodeId;
        private String linkType;
        private String name;
        private String condition;
    }
}