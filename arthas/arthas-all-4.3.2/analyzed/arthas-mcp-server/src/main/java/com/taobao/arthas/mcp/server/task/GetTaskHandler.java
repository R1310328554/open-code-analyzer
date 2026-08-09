/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.server.McpNettyServerExchange;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.util.concurrent.CompletableFuture;

/**
 * {@code tasks/get} 的可选自定义处理器。
 * <p>
 * 若返回非 null，{@link DefaultTaskManager} 将直接使用该结果；否则回退到 TaskStore 查询。
 *
 * @author Yeaury
 */
@FunctionalInterface
public interface GetTaskHandler {

    CompletableFuture<McpSchema.GetTaskResult> handle(
        McpNettyServerExchange exchange,
        McpSchema.GetTaskRequest request
    );
}
