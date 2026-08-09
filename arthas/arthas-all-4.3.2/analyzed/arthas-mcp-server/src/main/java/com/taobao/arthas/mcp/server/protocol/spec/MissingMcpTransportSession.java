/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.CompletableFuture;

/**
 * 占位 {@link McpSession}：表示当前无可用 SSE/HTTP 传输流。
 * <p>
 * 会话尚未建立 listening stream 或流已关闭时，
 * {@link McpStreamableServerSession} 将委托此类，使出站操作快速失败而非空指针。
 */
public class MissingMcpTransportSession implements McpSession {

    /** 关联的 MCP 会话 ID，用于错误信息。 */
    private final String sessionId;

    /** 绑定会话 ID 构造占位实例。 */
    public MissingMcpTransportSession(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    /** 无可用流时以 {@link IllegalStateException} 完成异常 Future。 */
    public <T> CompletableFuture<T> sendRequest(String method, Object requestParams, TypeReference<T> typeRef) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(
                new IllegalStateException("Stream unavailable for session " + this.sessionId)
        );
        return future;
    }

    @Override
    /** 无可用流时以 {@link IllegalStateException} 完成异常 Future。 */
    public CompletableFuture<Void> sendNotification(String method, Object params) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(
                new IllegalStateException("Stream unavailable for session " + this.sessionId)
        );
        return future;
    }

    @Override
    /** 占位会话无需释放资源，直接返回已完成的 Future。 */
    public CompletableFuture<Void> closeGracefully() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void close() {
        // 占位对象无底层连接，关闭为空操作
    }


}
