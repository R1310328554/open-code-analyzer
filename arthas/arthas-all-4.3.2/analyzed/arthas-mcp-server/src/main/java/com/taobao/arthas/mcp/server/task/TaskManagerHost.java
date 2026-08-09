/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.util.concurrent.CompletableFuture;

/**
 * {@link TaskManager} 与 MCP 协议层之间的通信桥接接口。
 * <p>
 * 提供会话级 request/notification 发送，以及 Task 相关 method 的处理器注册与自定义分发。
 *
 * @author Yeaury
 */
public interface TaskManagerHost {

    <T extends McpSchema.Result> CompletableFuture<T> request(McpSchema.Request request, Class<T> resultType);

    CompletableFuture<Void> notification(String notificationMethod, Object notification);

    /** 注册 Task 相关 JSON-RPC 方法的处理器（如 tasks/get）。 */
    void registerHandler(String method, TaskRequestHandler handler);

    /**
     * 若该 Task 对应工具有自定义处理器，则委托调用；否则返回 null。
     */
    <T extends McpSchema.Result> CompletableFuture<T> invokeCustomTaskHandler(
            String taskId, String method, McpSchema.Request request,
            TaskHandlerContext context, Class<T> resultType);

    /** Task JSON-RPC 请求的统一处理函数式接口。 */
    @FunctionalInterface
    interface TaskRequestHandler {
        CompletableFuture<McpSchema.Result> handle(String requestMethod, Object requestParams,
                                                    TaskHandlerContext context);
    }

    /** Task 处理器执行上下文：会话 ID 及向客户端发 request/notification 的能力。 */
    interface TaskHandlerContext {
        String sessionId();

        <T extends McpSchema.Result> CompletableFuture<T> sendRequest(String method, Object params,
                                                                       Class<T> resultType);

        CompletableFuture<Void> sendNotification(String method, Object notification);
    }
}
