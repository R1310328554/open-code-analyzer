/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Task 创建处理器：启动异步工作后立即返回 {@link McpSchema.CreateTaskResult}。
 * <p>
 * 典型流程：在 {@link CreateTaskContext} 上 createTask，后台线程执行 Arthas 命令，
 * 完成后调用 completeTask 或 failTask。
 *
 * @author Yeaury
 */
@FunctionalInterface
public interface CreateTaskHandler {

    CompletableFuture<McpSchema.CreateTaskResult> createTask(
        Map<String, Object> args,
        CreateTaskContext context
    );
}
