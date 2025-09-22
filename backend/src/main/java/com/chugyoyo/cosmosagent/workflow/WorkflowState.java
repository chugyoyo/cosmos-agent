package com.chugyoyo.cosmosagent.workflow;

public enum WorkflowState {
    IDLE,           // 空闲状态
    INITIALIZING,   // 初始化中
    RUNNING,        // 运行中
    PAUSED,         // 暂停
    COMPLETED,      // 完成
    FAILED,         // 失败
    CANCELLED       // 取消
}