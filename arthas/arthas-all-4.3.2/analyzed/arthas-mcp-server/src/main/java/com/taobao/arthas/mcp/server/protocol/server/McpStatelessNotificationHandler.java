/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server;


import java.util.concurrent.CompletableFuture;

/**
 * 无状态 MCP 服务端上的通知处理器。
 * <p>
 * 客户端发送 JSON-RPC 通知（无 id）时，由 {@link McpStatelessServerHandler} 路由至此接口。
 */
public interface McpStatelessNotificationHandler {

	/**
	 * 处理通知并在完成后结束 Future。
	 * @param transportContext 与本次传输关联的 {@link McpTransportContext}
	 * @param params MCP 通知负载
	 * @return 处理完成后完成的 CompletableFuture
	 */
	CompletableFuture<Void> handle(McpTransportContext transportContext, Object params);

}
