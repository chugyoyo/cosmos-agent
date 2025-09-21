package com.chugyoyo.cosmosagent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

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
    private String linkType;

    private String config;
}
