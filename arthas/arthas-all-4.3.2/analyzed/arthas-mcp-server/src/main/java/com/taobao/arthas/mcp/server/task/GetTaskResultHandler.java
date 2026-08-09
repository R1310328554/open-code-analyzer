/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.server.McpNettyServerExchange;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.util.concurrent.CompletableFuture;

/**
 * {@code tasks/result} 的可选自定义处理器。
 * <p>
 * 用于在默认轮询/取 payload 逻辑之前注入工具专属的结果组装行为。
 *
 * @author Yeaury
 */
@FunctionalInterface
public interface GetTaskResultHandler {

    CompletableFuture<McpSchema.ServerTaskPayloadResult> handle(
        McpNettyServerExchange exchange,
        McpSchema.GetTaskPayloadRequest request
    );
}
