/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server;

import com.taobao.arthas.mcp.server.session.ArthasCommandContext;

import java.util.concurrent.CompletableFuture;

/**
 * 无状态 MCP 服务端上的请求处理器。
 * <p>
 * 每个 MCP 方法（如 initialize、tools/call）对应一个 {@code McpStatelessRequestHandler} 实现。
 */
public interface McpStatelessRequestHandler<R> {

	/**
	 * 处理请求并返回结果。
	 * @param transportContext 与本次传输关联的 {@link McpTransportContext}
	 * @param params MCP 请求负载
	 * @return 携带响应对象的 CompletableFuture
	 */
	CompletableFuture<R> handle(McpTransportContext transportContext, ArthasCommandContext arthasCommandContext, Object params);

}
