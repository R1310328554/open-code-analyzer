/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.util.concurrent.CompletableFuture;

/**
 * 处理 MCP 客户端初始化请求，基于 CompletableFuture 异步返回握手结果。
 * This is the Netty-specific version that doesn't depend on Reactor.
 */
public interface McpInitRequestHandler {

	/**
	 * 处理 initialize 请求，协商协议版本并返回服务端能力。
	 * @param initializeRequest the initialization request by the client
	 * @return a CompletableFuture that will emit the result of the initialization
	 */
	CompletableFuture<McpSchema.InitializeResult> handle(McpSchema.InitializeRequest initializeRequest);

}
