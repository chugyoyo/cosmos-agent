package com.chugyoyo.cosmosagent.workflow;

import com.chugyoyo.cosmosagent.dto.WorkflowExecutionResponse;

/**
 * 节点执行器接口
 * 不同类型的节点实现不同的执行逻辑
 */
public interface AgentNodeExecutor {

    /**
     * 执行节点
     *
     * @param context 执行上下文
     * @return 执行结果
     */
    WorkflowExecutionResponse.ExecutionStep execute(WorkflowContext context) throws Exception;

    /**
     * 获取支持的节点类型
     *
     * @return 节点类型
     */
    NodeType getSupportedNodeType();
}
