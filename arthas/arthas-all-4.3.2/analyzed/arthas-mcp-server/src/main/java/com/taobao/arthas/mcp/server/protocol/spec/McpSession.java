/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 服务端 MCP 会话抽象，管理与客户的双向 JSON-RPC 通信。
 * <p>
 * 会话可主动向客户端发起请求/通知，并在关闭时释放关联传输资源。
 *
 * @author Yeaury
 */
public interface McpSession {

	/** 向客户端发送 JSON-RPC 请求并异步等待 typed 结果。 */
	<T> CompletableFuture<T> sendRequest(String method, Object requestParams, TypeReference<T> typeRef);

	/** 发送无参数通知的便捷重载。 */
	default CompletableFuture<Void> sendNotification(String method) {
		return sendNotification(method, null);
	}

	/** 向客户端发送 JSON-RPC 通知（无需响应）。 */
	CompletableFuture<Void> sendNotification(String method, Object params);

	/** 优雅关闭会话，等待未完成操作结束。 */
	CompletableFuture<Void> closeGracefully();

	/** 立即关闭会话，不保证等待在途请求。 */
	void close();

}
