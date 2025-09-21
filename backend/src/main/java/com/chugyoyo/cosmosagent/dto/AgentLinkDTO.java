package com.chugyoyo.cosmosagent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentLinkDTO {

    private Long id;

    @NotNull(message = "代理ID不能为空")
    private Long agentId;

    @NotNull(message = "源节点ID不能为空")
    private Long sourceNodeId;

    @NotNull(message = "目标节点ID不能为空")
    private Long targetNodeId;

    @NotBlank(message = "链接类型不能为空")
    @Size(max = 50, message = "链接类型长度不能超过50个字符")
    private String linkType;

    @Size(max = 100, message = "链接名称长度不能超过100个字符")
    private String name;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @Size(max = 200, message = "条件表达式长度不能超过200个字符")
    private String condition;

    private Integer sortOrder;

    private Integer status;

    private String config;
}
