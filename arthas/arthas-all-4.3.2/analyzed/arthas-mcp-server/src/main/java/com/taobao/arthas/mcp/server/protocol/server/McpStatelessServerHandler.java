/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.util.concurrent.CompletableFuture;

/**
 * 无状态 MCP 服务端顶层处理器，负责 JSON-RPC 请求与通知的分发。
 */
public interface McpStatelessServerHandler {

	/**
	 * 处理 JSON-RPC 请求并返回响应。
	 * @param transportContext 携带传输层元数据的 {@link McpTransportContext}
	 * @param request JSON-RPC 请求对象
	 * @return 包含 JSON-RPC 响应的 CompletableFuture
	 */
	CompletableFuture<McpSchema.JSONRPCResponse> handleRequest(McpTransportContext transportContext,
												  McpSchema.JSONRPCRequest request);

	/**
	 * 处理 JSON-RPC 通知（无响应体，HTTP 层通常返回 202）。
	 * @param transportContext 携带传输层元数据的 {@link McpTransportContext}
	 * @param notification JSON-RPC 通知对象
	 * @return 处理完成后完成的 CompletableFuture
	 */
	CompletableFuture<Void> handleNotification(McpTransportContext transportContext, McpSchema.JSONRPCNotification notification);

}
