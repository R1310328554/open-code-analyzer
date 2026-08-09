/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Task 相关 JSON-RPC 请求处理器的注册表，客户端与服务端共用。
 * <p>
 * 维护 method 到 {@link TaskManagerHost.TaskRequestHandler} 的映射，
 * 并支持按能力标志装配 tasks/get、tasks/result、tasks/list、tasks/cancel。
 *
 * @author Yeaury
 */
class TaskHandlerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(TaskHandlerRegistry.class);

    private final ConcurrentHashMap<String, TaskManagerHost.TaskRequestHandler> handlers = new ConcurrentHashMap<>();

    public void registerHandler(String method, TaskManagerHost.TaskRequestHandler handler) {
        logger.debug("Registered task handler: {}", method);
        this.handlers.put(method, handler);
    }

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> invokeHandler(String method, Object params,
                                                   TaskManagerHost.TaskHandlerContext context) {
        TaskManagerHost.TaskRequestHandler handler = this.handlers.get(method);
        if (handler == null) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("No handler registered: " + method));
            return future;
        }
        return handler.handle(method, params, context)
                .thenApply(result -> (T) result);
    }

    /**
     * 通过 adapter/registrar 将已注册处理器接入协议层。
     * tasks/get 与 tasks/result 始终装配；tasks/list、tasks/cancel 按能力标志条件装配。
     */
    public <T> void wireHandlers(boolean supportsList, boolean supportsCancel,
                                  BiFunction<String, TaskManagerHost.TaskRequestHandler, T> adapter,
                                  BiConsumer<String, T> registrar) {
        wireIfPresent(McpSchema.METHOD_TASKS_GET, adapter, registrar);
        wireIfPresent(McpSchema.METHOD_TASKS_RESULT, adapter, registrar);
        if (supportsList) {
            wireIfPresent(McpSchema.METHOD_TASKS_LIST, adapter, registrar);
        }
        if (supportsCancel) {
            wireIfPresent(McpSchema.METHOD_TASKS_CANCEL, adapter, registrar);
        }
    }

    private <T> void wireIfPresent(String method,
                                    BiFunction<String, TaskManagerHost.TaskRequestHandler, T> adapter,
                                    BiConsumer<String, T> registrar) {
        TaskManagerHost.TaskRequestHandler handler = this.handlers.get(method);
        if (handler != null) {
            registrar.accept(method, adapter.apply(method, handler));
        } else {
            logger.warn("No handler registered for {}", method);
        }
    }
}
