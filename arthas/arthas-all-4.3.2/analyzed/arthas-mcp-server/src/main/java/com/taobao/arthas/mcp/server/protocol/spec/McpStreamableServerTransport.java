/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.CompletableFuture;

/**
 * 服务端可流式 MCP 传输标记接口。
 * <p>
 * 在 {@link McpServerTransport} 基础上支持带 {@code messageId} 的消息发送，
 * 以便 SSE 事件存储与 Last-Event-ID 重播。
 */
public interface McpStreamableServerTransport extends McpServerTransport {

    /** 发送 JSON-RPC 消息；{@code messageId} 非空时写入 SSE 事件 ID。 */
    CompletableFuture<Void> sendMessage(McpSchema.JSONRPCMessage message, String messageId);

    /** 将 JSON-RPC result 反序列化为指定 Java 类型。 */
    <T> T unmarshalFrom(Object value, TypeReference<T> typeRef);
}
