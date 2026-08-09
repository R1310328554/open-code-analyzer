/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

/**
 * {@link TaskStore#getTask} 的查询结果，携带 Task 快照及创建时的原始 MCP 请求。
 * <p>
 * 自定义 {@link GetTaskHandler} / {@link GetTaskResultHandler} 可据此还原调用上下文。
 *
 * @author Yeaury
 */
public class GetTaskFromStoreResult {

    private final McpSchema.Task task;
    private final McpSchema.Request originatingRequest;

    /** @param task 当前 Task 状态 @param originatingRequest 创建 Task 时的原始请求 */
    public GetTaskFromStoreResult(McpSchema.Task task, McpSchema.Request originatingRequest) {
        this.task = task;
        this.originatingRequest = originatingRequest;
    }

    public McpSchema.Task task() {
        return task;
    }

    public McpSchema.Request originatingRequest() {
        return originatingRequest;
    }
}
