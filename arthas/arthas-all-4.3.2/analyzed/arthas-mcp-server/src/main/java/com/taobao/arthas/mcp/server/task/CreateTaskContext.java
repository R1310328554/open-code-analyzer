/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.server.McpNettyServerExchange;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.session.ArthasCommandContext;
import com.taobao.arthas.mcp.server.session.ArthasCommandSessionManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 创建 Task 时注入的生命周期上下文，供 {@link CreateTaskHandler} 访问会话、存储与编排能力。
 * <p>
 * 封装 {@link TaskStore} 写入、隔离 Arthas session 创建及并发上限检查等操作。
 *
 * @author Yeaury
 */
public interface CreateTaskContext {

    /** 当前 MCP 交换上下文（含传输层元数据）。 */
    McpNettyServerExchange exchange();

    /** 发起 tools/call 的 MCP 会话 ID。 */
    String sessionId();

    /** 客户端请求的 Task TTL（毫秒），可为 null 使用默认值。 */
    Long requestTtl();

    /** 触发 Task 创建的原始 JSON-RPC 请求。 */
    McpSchema.Request originatingRequest();

    /** 与 MCP 会话共享的命令执行上下文。 */
    ArthasCommandContext commandContext();

    /** 以默认选项在 TaskStore 中登记新 Task。 */
    CompletableFuture<McpSchema.Task> createTask();

    /** 通过 Builder 自定义 taskId、pollInterval 等后创建 Task。 */
    CompletableFuture<McpSchema.Task> createTask(Consumer<CreateTaskOptions.Builder> customizer);

    /** 将 Task 标记为 COMPLETED 并写入工具调用结果。 */
    CompletableFuture<Void> completeTask(String taskId, McpSchema.CallToolResult result);

    /** 将 Task 标记为 FAILED 并写入错误结果。 */
    CompletableFuture<Void> failTask(String taskId, McpSchema.CallToolResult errorResult);

    /** 将 Task 置为 INPUT_REQUIRED，等待客户端通过 side-channel 补充输入。 */
    CompletableFuture<Void> setInputRequired(String taskId, String message);

    /** 查询客户端是否已请求取消该 Task。 */
    CompletableFuture<Boolean> isCancellationRequested(String taskId);

    /** 会话管理器，用于创建/清理 Task 隔离 session。 */
    ArthasCommandSessionManager sessionManager();

    /** 为指定 Task 创建独立 Arthas session 并注入当前认证信息。 */
    ArthasCommandContext createIsolatedTaskSession(String taskId);

    /** Task 结束后关闭并移除对应的隔离 session。 */
    void cleanupTaskSession(String taskId);

    /** 当前 Task 隔离 session 数是否已达并发上限。 */
    boolean isAtConcurrencyLimit();
}
