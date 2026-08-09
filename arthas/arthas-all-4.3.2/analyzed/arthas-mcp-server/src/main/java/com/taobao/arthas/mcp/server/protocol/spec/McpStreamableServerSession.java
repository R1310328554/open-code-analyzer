/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.taobao.arthas.mcp.server.CommandExecutor;
import com.taobao.arthas.mcp.server.protocol.server.McpNettyServerExchange;

import com.taobao.arthas.mcp.server.protocol.server.McpNotificationHandler;
import com.taobao.arthas.mcp.server.protocol.server.McpRequestHandler;
import com.taobao.arthas.mcp.server.protocol.server.McpTransportContext;
import com.taobao.arthas.mcp.server.session.ArthasCommandContext;
import com.taobao.arthas.mcp.server.session.ArthasCommandSessionManager;
import com.taobao.arthas.mcp.server.task.TaskMessageQueue;
import com.taobao.arthas.mcp.server.task.TaskStore;
import com.taobao.arthas.mcp.server.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.taobao.arthas.mcp.server.util.McpAuthExtractor.MCP_AUTH_SUBJECT_KEY;

/**
 * 可流式 MCP 服务端会话的核心实现。
 * <p>
 * 维护会话 ID、客户端能力、事件存储与多条并发 SSE 流；
 * 通过 {@link CompletableFuture} 异步处理 JSON-RPC 请求、通知与出站调用。
 */
public class McpStreamableServerSession implements McpSession {

    /** 会话级日志记录器。 */
    private static final Logger logger = LoggerFactory.getLogger(McpStreamableServerSession.class);

    /** 出站请求 ID 到承载该请求的 SSE 子流的映射。 */
    private final ConcurrentHashMap<Object, McpStreamableServerSessionStream> requestIdToStream = new ConcurrentHashMap<>();

    /** 全局唯一的会话标识，亦写入 HTTP 头 mcp-session-id。 */
    private final String id;
    /** 出站请求等待客户端响应的超时时间。 */
    private final Duration requestTimeout;
    /** 会话内出站请求序号生成器。 */
    private final AtomicLong requestCounter = new AtomicLong(0);
    /** 入站 JSON-RPC 请求的方法名到处理器映射。 */
    private final Map<String, McpRequestHandler<?>> requestHandlers;
    /** 入站 JSON-RPC 通知的方法名到处理器映射。 */
    private final Map<String, McpNotificationHandler> notificationHandlers;
    
    /** 客户端在 initialize 中声明的能力快照。 */
    private final AtomicReference<McpSchema.ClientCapabilities> clientCapabilities = new AtomicReference<>();

    /** 客户端名称与版本信息。 */
    private final AtomicReference<McpSchema.Implementation> clientInfo = new AtomicReference<>();

    /** 当前用于接收服务端主动推送的 SSE 监听流。 */
    private final AtomicReference<McpSession> listeningStreamRef;

    /** 无可用监听流时的占位委托对象。 */
    private final MissingMcpTransportSession missingMcpTransportSession;
    
    /** 允许向客户端推送的最低日志级别。 */
    private volatile McpSchema.LoggingLevel minLoggingLevel = McpSchema.LoggingLevel.INFO;

    /** Arthas 命令执行器。 */
    private final CommandExecutor commandExecutor;

    /** 按 MCP 会话绑定 Arthas 命令会话的管理器。 */
    private final ArthasCommandSessionManager commandSessionManager;
    
    /** 会话级 JSON-RPC 事件存储，支持 SSE 重播。 */
    private final EventStore eventStore;

    /** 长运行任务的状态与结果存储。 */
    private final TaskStore<McpSchema.ServerTaskPayloadResult> taskStore;

    /** 任务进度与中间结果的异步消息队列。 */
    private final TaskMessageQueue taskMessageQueue;

    /**
     * 构造会话实例并初始化命令会话管理器与占位监听流。
     */
    public McpStreamableServerSession(String id, McpSchema.ClientCapabilities clientCapabilities,
                                      McpSchema.Implementation clientInfo, Duration requestTimeout,
                                      Map<String, McpRequestHandler<?>> requestHandlers,
                                      Map<String, McpNotificationHandler> notificationHandlers,
                                      CommandExecutor commandExecutor, EventStore eventStore,
                                      TaskStore<McpSchema.ServerTaskPayloadResult> taskStore, TaskMessageQueue taskMessageQueue) {
        this.id = id;
        this.missingMcpTransportSession = new MissingMcpTransportSession(id);
        this.listeningStreamRef = new AtomicReference<>(this.missingMcpTransportSession);
        this.clientCapabilities.lazySet(clientCapabilities);
        this.clientInfo.lazySet(clientInfo);
        this.requestTimeout = requestTimeout;
        this.requestHandlers = requestHandlers;
        this.notificationHandlers = notificationHandlers;
        this.commandExecutor = commandExecutor;
        this.commandSessionManager = new ArthasCommandSessionManager(commandExecutor);
        this.eventStore = eventStore;
        this.taskStore = taskStore;
        this.taskMessageQueue = taskMessageQueue;
    }

    /**
     * 设置本会话允许推送的最低日志级别。
     * @param minLoggingLevel the minimum logging level
     */
    public void setMinLoggingLevel(McpSchema.LoggingLevel minLoggingLevel) {
        Assert.notNull(minLoggingLevel, "minLoggingLevel must not be null");
        this.minLoggingLevel = minLoggingLevel;
    }

    /**
     * 判断给定级别是否不低于当前最低级别，从而是否允许推送日志通知。
     * @param loggingLevel the logging level to check
     * @return true if notifications for this level are allowed
     */
    public boolean isNotificationForLevelAllowed(McpSchema.LoggingLevel loggingLevel) {
        return loggingLevel.level() >= this.minLoggingLevel.level();
    }

    /** 返回会话唯一标识。 */
    public String getId() {
        return this.id;
    }

    /** 生成带会话前缀的出站请求 ID，保证全局可路由。 */
    private String generateRequestId() {
        return this.id + "-" + this.requestCounter.getAndIncrement();
    }

    @Override
    public <T> CompletableFuture<T> sendRequest(String method, Object requestParams, TypeReference<T> typeRef) {
        McpSession listeningStream = this.listeningStreamRef.get();
        return listeningStream.sendRequest(method, requestParams, typeRef);
    }

    @Override
    public CompletableFuture<Void> sendNotification(String method, Object params) {
        McpSession listeningStream = this.listeningStreamRef.get();
        return listeningStream.sendNotification(method, params);
    }

    /** 优雅关闭并清理事件存储与 Arthas 命令会话。 */
    public CompletableFuture<Void> delete() {
        return this.closeGracefully().thenRun(() -> {
            try {
                eventStore.removeSessionEvents(this.id);
                commandSessionManager.closeCommandSession(this.id);
            } catch (Exception e) {
                logger.warn("Failed to clear session during deletion: {}", e.getMessage());
            }
        });
    }

    /** 绑定 SSE 监听流，供服务端主动推送通知与请求。 */
    public McpStreamableServerSessionStream listeningStream(McpStreamableServerTransport transport) {
        McpStreamableServerSessionStream listeningStream = new McpStreamableServerSessionStream(transport);
        this.listeningStreamRef.set(listeningStream);
        return listeningStream;
    }

    /**
     * 重播会话事件，从指定的最后事件ID之后开始
     * 
     * @param lastEventId 最后一个事件ID，如果为null则从头开始重播
     * @return 事件消息流
     */
    public Stream<McpSchema.JSONRPCMessage> replay(Object lastEventId) {
        String lastEventIdStr = lastEventId != null ? lastEventId.toString() : null;
        
        return eventStore.getEventsForSession(this.id, lastEventIdStr)
                .map(EventStore.StoredEvent::getMessage);
    }

    /** 在独立 SSE 流上处理单次 JSON-RPC 请求并返回响应后关闭流。 */
    public CompletableFuture<Void> responseStream(McpSchema.JSONRPCRequest jsonrpcRequest, 
            McpStreamableServerTransport transport, McpTransportContext transportContext) {
        
        McpStreamableServerSessionStream stream = new McpStreamableServerSessionStream(transport);
        McpRequestHandler<?> requestHandler = this.requestHandlers.get(jsonrpcRequest.getMethod());
        
        if (requestHandler == null) {
            MethodNotFoundError error = getMethodNotFoundError(jsonrpcRequest.getMethod());
            McpSchema.JSONRPCResponse errorResponse = new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, 
                    jsonrpcRequest.getId(), null,
                    new McpSchema.JSONRPCResponse.JSONRPCError(McpSchema.ErrorCodes.METHOD_NOT_FOUND,
                            error.getMessage(), error.getData()));

            // 将 METHOD_NOT_FOUND 错误写入事件存储，便于 SSE 重播
            try {
                eventStore.storeEvent(this.id, errorResponse);
            } catch (Exception e) {
                logger.warn("Failed to store error response event: {}", e.getMessage());
            }

            return transport.sendMessage(errorResponse, null)
                    .thenCompose(v -> transport.closeGracefully());
        }
        ArthasCommandContext commandContext = createCommandContext(transportContext.get(MCP_AUTH_SUBJECT_KEY));

        return requestHandler
                .handle(new McpNettyServerExchange(this.id, stream, clientCapabilities.get(), 
                        clientInfo.get(), transportContext, taskMessageQueue, taskStore), 
                        commandContext, jsonrpcRequest.getParams())
                .handle((result, ex) -> {
                    if (ex != null) {
                        Throwable cause = ex;
                        if (cause instanceof java.util.concurrent.CompletionException) {
                            cause = cause.getCause();
                        }

                        McpSchema.JSONRPCResponse.JSONRPCError jsonRpcError;
                        if (cause instanceof McpError && ((McpError) cause).getJsonRpcError() != null) {
                            jsonRpcError = ((McpError) cause).getJsonRpcError();
                        } else {
                            jsonRpcError = new McpSchema.JSONRPCResponse.JSONRPCError(McpSchema.ErrorCodes.INTERNAL_ERROR,
                                    cause.getMessage(), McpError.aggregateExceptionMessages(cause));
                        }

                        return new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, jsonrpcRequest.getId(),
                                null, jsonRpcError);
                    } else {
                        return new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION,
                                jsonrpcRequest.getId(), result, null);
                    }
                })
                .thenCompose(response -> transport.sendMessage(response, null))
                .thenCompose(v -> transport.closeGracefully());
    }

    /** 分发入站 JSON-RPC 通知到已注册处理器。 */
    public CompletableFuture<Void> accept(McpSchema.JSONRPCNotification notification, 
            McpTransportContext transportContext) {
        
        McpNotificationHandler notificationHandler = this.notificationHandlers.get(notification.getMethod());
        if (notificationHandler == null) {
            logger.error("No handler registered for notification method: {}", notification.getMethod());
            return CompletableFuture.completedFuture(null);
        }

        ArthasCommandContext commandContext = createCommandContext(transportContext.get(MCP_AUTH_SUBJECT_KEY));
        McpSession listeningStream = this.listeningStreamRef.get();
        return notificationHandler.handle(new McpNettyServerExchange(this.id, listeningStream,
                this.clientCapabilities.get(), this.clientInfo.get(), transportContext, taskMessageQueue, taskStore), 
                commandContext, notification.getParams());
    }

    /** 将客户端对出站请求的 JSON-RPC 响应路由回对应 SSE 子流。 */
    public CompletableFuture<Void> accept(McpSchema.JSONRPCResponse response) {
        McpStreamableServerSessionStream stream = this.requestIdToStream.get(response.getId());
        if (stream == null) {
            CompletableFuture<Void> f = CompletableFuture.completedFuture(null);
            f.completeExceptionally(new McpError("Unexpected response for unknown id " + response.getId()));
            return f;
        }

        CompletableFuture<McpSchema.JSONRPCResponse> future = stream.pendingResponses.remove(response.getId());
        if (future == null) {
            CompletableFuture<Void> f = CompletableFuture.completedFuture(null);
            f.completeExceptionally(new McpError("Unexpected response for unknown id " + response.getId()));
            return f;
        } else {
            future.complete(response);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /** 方法未找到时的结构化错误载荷。 */
    public class MethodNotFoundError {
        private final String method;
        private final String message;
        private final Object data;

        public MethodNotFoundError(String method, String message, Object data) {
            this.method = method;
            this.message = message;
            this.data = data;
        }

        public String getMethod() {
            return method;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }


    /** 构造标准 METHOD_NOT_FOUND 错误描述。 */
    private MethodNotFoundError getMethodNotFoundError(String method) {
        return new MethodNotFoundError(method, "Method not found: " + method, null);
    }

    @Override
    public CompletableFuture<Void> closeGracefully() {
        McpSession listeningStream = this.listeningStreamRef.getAndSet(missingMcpTransportSession);
        
        // 清理 Arthas 命令会话
        try {
            commandSessionManager.closeCommandSession(this.id);
            logger.debug("Successfully closed command session during graceful shutdown: {}", this.id);
        } catch (Exception e) {
            logger.warn("Failed to close command session during graceful shutdown: {}", e.getMessage());
        }
        
        return listeningStream.closeGracefully();
        // TODO: 同步关闭所有仍打开的 SSE 子流
    }

    @Override
    public void close() {
        McpSession listeningStream = this.listeningStreamRef.getAndSet(missingMcpTransportSession);
        
        // 清理 Arthas 命令会话
        try {
            commandSessionManager.closeCommandSession(this.id);
            logger.debug("Successfully closed command session during close: {}", this.id);
        } catch (Exception e) {
            logger.warn("Failed to close command session during close: {}", e.getMessage());
        }
        
        if (listeningStream != null) {
            listeningStream.close();
        }
        // TODO: 同步关闭所有仍打开的 SSE 子流
    }

    /** 由传输层调用以创建并初始化新会话。 */
    public interface Factory {
        McpStreamableServerSessionInit startSession(McpSchema.InitializeRequest initializeRequest);
    }

    /** {@link #startSession} 的返回值：会话实例与 initialize 异步结果。 */
    public static class McpStreamableServerSessionInit {
        private final McpStreamableServerSession session;
        private final CompletableFuture<McpSchema.InitializeResult> initResult;

        public McpStreamableServerSessionInit(
                McpStreamableServerSession session,
                CompletableFuture<McpSchema.InitializeResult> initResult) {
            this.session = session;
            this.initResult = initResult;
        }

        public McpStreamableServerSession session() {
            return session;
        }

        public CompletableFuture<McpSchema.InitializeResult> initResult() {
            return initResult;
        }
    }


    /** 绑定单条 SSE 传输的子会话，负责该流上的出站 RPC 与 pending 响应表。 */
    public final class McpStreamableServerSessionStream implements McpSession {

        /** 等待客户端响应的出站请求 ID 到 Future 的映射。 */
        private final ConcurrentHashMap<Object, CompletableFuture<McpSchema.JSONRPCResponse>> pendingResponses = new ConcurrentHashMap<>();

        /** 本流绑定的可流式传输实现。 */
        private final McpStreamableServerTransport transport;
        /** 本子流的稳定前缀 ID，便于按流过滤事件历史。 */
        private final String transportId;
        /** 生成本流内唯一消息 ID 的供应商。 */
        private final Supplier<String> uuidGenerator;

        public McpStreamableServerSessionStream(McpStreamableServerTransport transport) {
            this.transport = transport;
            this.transportId = UUID.randomUUID().toString();
            // transportId 作为 SSE 事件 ID 前缀，便于按流 O(1) 定位历史
            this.uuidGenerator = () -> this.transportId + "_" + UUID.randomUUID();
        }

        @Override
        public <T> CompletableFuture<T> sendRequest(String method, Object requestParams, TypeReference<T> typeRef) {
            String requestId = McpStreamableServerSession.this.generateRequestId();

            McpStreamableServerSession.this.requestIdToStream.put(requestId, this);

            CompletableFuture<McpSchema.JSONRPCResponse> responseFuture = new CompletableFuture<>();
            this.pendingResponses.put(requestId, responseFuture);

            McpSchema.JSONRPCRequest jsonrpcRequest = new McpSchema.JSONRPCRequest(McpSchema.JSONRPC_VERSION,
                    method, requestId, requestParams);
            String messageId = null;
            
            // 存储发送的请求到事件存储
            try {
                messageId = McpStreamableServerSession.this.eventStore.storeEvent(
                    McpStreamableServerSession.this.id, jsonrpcRequest);
            } catch (Exception e) {
                logger.warn("Failed to store outbound request event: {}", e.getMessage());
            }

            // 经传输层发送请求；发送失败则异常完成 responseFuture
            this.transport.sendMessage(jsonrpcRequest, messageId).exceptionally(ex -> {
                responseFuture.completeExceptionally(ex);
                return null;
            });

            return responseFuture.handle((jsonRpcResponse, throwable) -> {
                // 无论成功失败均清理 pending 映射，避免泄漏
                this.pendingResponses.remove(requestId);
                McpStreamableServerSession.this.requestIdToStream.remove(requestId);

                if (throwable != null) {
                    if (throwable instanceof RuntimeException) {
                        throw (RuntimeException) throwable;
                    }
                    throw new RuntimeException(throwable);
                }

                if (jsonRpcResponse.getError() != null) {
                    throw new RuntimeException(new McpError(jsonRpcResponse.getError()));
                } else {
                    if (typeRef.getType().equals(Void.class)) {
                        return null;
                    } else {
                        return this.transport.unmarshalFrom(jsonRpcResponse.getResult(), typeRef);
                    }
                }
            });
        }

        @Override
        public CompletableFuture<Void> sendNotification(String method, Object params) {
            McpSchema.JSONRPCNotification jsonrpcNotification = new McpSchema.JSONRPCNotification(
                    McpSchema.JSONRPC_VERSION, method, params);
            String messageId = null;
            try {
                messageId = McpStreamableServerSession.this.eventStore.storeEvent(
                        McpStreamableServerSession.this.id, jsonrpcNotification);
            } catch (Exception e) {
                logger.warn("Failed to store outbound notification event: {}", e.getMessage());
            }

            return this.transport.sendMessage(jsonrpcNotification, messageId);
        }

        @Override
        public CompletableFuture<Void> closeGracefully() {
            // 关闭时将全部 pending 请求以异常完成
            this.pendingResponses.values().forEach(future -> 
                    future.completeExceptionally(new RuntimeException("Stream closed")));
            this.pendingResponses.clear();
            
            // 若关闭的是当前监听流，则回退为占位会话
            McpStreamableServerSession.this.listeningStreamRef.compareAndSet(this,
                    McpStreamableServerSession.this.missingMcpTransportSession);

            McpStreamableServerSession.this.requestIdToStream.values().removeIf(this::equals);
            
            return this.transport.closeGracefully();
        }

        @Override
        public void close() {
            this.pendingResponses.values().forEach(future -> 
                    future.completeExceptionally(new RuntimeException("Stream closed")));
            this.pendingResponses.clear();
            
            // If this was the generic stream, reset it
            McpStreamableServerSession.this.listeningStreamRef.compareAndSet(this,
                    McpStreamableServerSession.this.missingMcpTransportSession);
            McpStreamableServerSession.this.requestIdToStream.values().removeIf(this::equals);
            
            this.transport.close();
        }


    }

    /**
     * 创建命令执行上下文
     *
     * @return 命令执行上下文
     */
    private ArthasCommandContext createCommandContext(Object authSubject) {
        ArthasCommandSessionManager.CommandSessionBinding binding = commandSessionManager.getCommandSession(this.id, authSubject);
        return new ArthasCommandContext(commandExecutor, binding);
    }
}
