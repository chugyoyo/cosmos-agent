package com.chugyoyo.cosmosagent.workflow;

public enum NodeState {
    PENDING,        // 等待执行
    RUNNING,        // 执行中
    COMPLETED,      // 完成
    FAILED,         // 失败
    SKIPPED         // 跳过
}