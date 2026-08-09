/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server;

import com.taobao.arthas.mcp.server.session.ArthasCommandContext;

import java.util.concurrent.CompletableFuture;

/**
 * 处理 MCP 客户端 JSON-RPC 请求（tools/list、tools/call 等），返回异步结果。
 * This is the Netty-specific version that doesn't depend on Reactor.
 */
public interface McpRequestHandler<T> {

	/**
	 * 处理客户端 request 并产生响应 payload。
	 * @param exchange the exchange associated with the client that allows calling back to
	 * the connected client or inspecting its capabilities.
	 * @param params the parameters of the request.
	 * @return a CompletableFuture that will emit the response to the request.
	 */
	CompletableFuture<T> handle(McpNettyServerExchange exchange, ArthasCommandContext arthasCommandContext, Object params);

}
