/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import com.taobao.arthas.mcp.server.CommandExecutor;
import com.taobao.arthas.mcp.server.protocol.server.McpInitRequestHandler;
import com.taobao.arthas.mcp.server.protocol.server.McpNotificationHandler;
import com.taobao.arthas.mcp.server.protocol.server.McpRequestHandler;
import com.taobao.arthas.mcp.server.protocol.server.store.InMemoryEventStore;
import com.taobao.arthas.mcp.server.task.TaskMessageQueue;
import com.taobao.arthas.mcp.server.task.TaskStore;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 可流式 MCP 服务端会话工厂的默认实现。
 * <p>
 * 根据注入的超时、请求/通知处理器、命令执行器与任务组件，
 * 为每次 {@link McpSchema.InitializeRequest} 创建独立的 {@link McpStreamableServerSession}。
 */
public class DefaultMcpStreamableServerSessionFactory implements McpStreamableServerSession.Factory {

    /** 单次 JSON-RPC 请求的超时时间。 */
    private final Duration requestTimeout;
    /** 处理 initialize 握手并返回 {@link McpSchema.InitializeResult} 的处理器。 */
    private final McpInitRequestHandler mcpInitRequestHandler;
    /** 方法名到请求处理器的映射表。 */
    private final Map<String, McpRequestHandler<?>> requestHandlers;
    /** 方法名到通知处理器的映射表。 */
    private final Map<String, McpNotificationHandler> notificationHandlers;
    /** 执行 Arthas 命令的底层执行器。 */
    private final CommandExecutor commandExecutor;
    /** 持久化服务端任务状态与结果的存储。 */
    private final TaskStore<McpSchema.ServerTaskPayloadResult> taskStore;
    /** 任务相关消息的异步队列。 */
    private final TaskMessageQueue taskMessageQueue;

    /**
     * 构造会话工厂，注入运行会话所需的全部依赖。
     */
    public DefaultMcpStreamableServerSessionFactory(Duration requestTimeout,
                                                    McpInitRequestHandler mcpInitRequestHandler,
                                                    Map<String, McpRequestHandler<?>> requestHandlers,
                                                    Map<String, McpNotificationHandler> notificationHandlers,
                                                    CommandExecutor commandExecutor,
                                                    TaskStore<McpSchema.ServerTaskPayloadResult> taskStore,
                                                    TaskMessageQueue taskMessageQueue) {
        this.requestTimeout = requestTimeout;
        this.mcpInitRequestHandler = mcpInitRequestHandler;
        this.requestHandlers = requestHandlers;
        this.notificationHandlers = notificationHandlers;
        this.commandExecutor = commandExecutor;
        this.taskStore = taskStore;
        this.taskMessageQueue = taskMessageQueue;
    }

    @Override
    public McpStreamableServerSession.McpStreamableServerSessionInit startSession(
            McpSchema.InitializeRequest initializeRequest) {

        // 以随机 UUID 作为会话标识，并绑定内存事件存储
        McpStreamableServerSession session = new McpStreamableServerSession(
                UUID.randomUUID().toString(),
                initializeRequest.getCapabilities(),
                initializeRequest.getClientInfo(),
                requestTimeout,
                requestHandlers,
                notificationHandlers,
                commandExecutor,
                new InMemoryEventStore(),
                taskStore,
                taskMessageQueue);

        // 异步处理 initialize 请求，结果与 session 一并封装返回
        CompletableFuture<McpSchema.InitializeResult> initResult = 
                this.mcpInitRequestHandler.handle(initializeRequest);

        return new McpStreamableServerSession.McpStreamableServerSessionInit(session, initResult);
    }
}
