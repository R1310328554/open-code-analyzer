/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * MCP 传输层基础抽象：发送 JSON-RPC 消息与反序列化响应数据。
 * <p>
 * 具体实现（Netty HTTP/SSE 等）负责编解码与连接生命周期管理。
 *
 * @author Yeaury
 */
public interface McpTransport {

	/** 等待在途发送完成后关闭传输。 */
	CompletableFuture<Void> closeGracefully();

	/** 默认委托 {@link #closeGracefully()}。 */
	default void close() {
		this.closeGracefully();
	}

	/** 异步发送一条 JSON-RPC 消息。 */
	CompletableFuture<Void> sendMessage(McpSchema.JSONRPCMessage message);

	/** 将 JSON 结构反序列化为目标类型。 */
	<T> T unmarshalFrom(Object data, TypeReference<T> typeRef);

}
