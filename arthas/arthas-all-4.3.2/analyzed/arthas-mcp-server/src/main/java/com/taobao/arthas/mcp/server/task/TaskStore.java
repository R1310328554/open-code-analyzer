/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 带会话隔离的 Task 状态与结果持久化存储。
 * <p>
 * 会话校验：{@code sessionId} 为 null 时允许全部访问（单租户）；
 * 无 session 的 Task 任意会话可读；否则 sessionId 必须匹配。
 * <p>
 * 错误约定：{@link #getTask}、{@link #getTaskResult} 未命中返回 null；
 * {@link #storeTaskResult} 未命中抛异常；{@link #updateTaskStatus} 静默忽略；
 * {@link #requestCancellation} 对终态 Task 抛 -32602。
 *
 * @param <R> 本存储保存的结果类型
 * @author Yeaury
 */
public interface TaskStore<R extends McpSchema.Result> {

    CompletableFuture<McpSchema.Task> createTask(CreateTaskOptions options);

    CompletableFuture<GetTaskFromStoreResult> getTask(String taskId, String sessionId);

    CompletableFuture<Void> updateTaskStatus(String taskId, String sessionId,
                                              McpSchema.TaskStatus status, String statusMessage);

    CompletableFuture<Void> storeTaskResult(String taskId, String sessionId,
                                             McpSchema.TaskStatus status, R result);

    CompletableFuture<R> getTaskResult(String taskId, String sessionId);

    CompletableFuture<McpSchema.ListTasksResult> listTasks(String cursor, String sessionId);

    CompletableFuture<McpSchema.Task> requestCancellation(String taskId, String sessionId);

    CompletableFuture<Boolean> isCancellationRequested(String taskId, String sessionId);

    CompletableFuture<List<McpSchema.Task>> watchTaskUntilTerminal(
            String taskId, String sessionId, long timeoutMs);

    default CompletableFuture<Void> shutdown() {
        return CompletableFuture.completedFuture(null);
    }
}
