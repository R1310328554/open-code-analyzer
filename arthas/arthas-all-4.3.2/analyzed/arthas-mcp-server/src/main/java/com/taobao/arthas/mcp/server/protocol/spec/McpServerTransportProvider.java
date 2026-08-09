/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import com.taobao.arthas.mcp.server.protocol.server.handler.McpStreamableHttpRequestHandler;

import java.util.concurrent.CompletableFuture;

/**
 * MCP 服务端传输提供者的抽象。
 * <p>
 * 负责向已连接客户端广播通知、优雅关闭传输层，
 * 并暴露 {@link McpStreamableHttpRequestHandler} 供 Netty 管线挂载。
 *
 * @author Yeaury
 */
public interface McpServerTransportProvider {

	/** 向所有活跃客户端发送 JSON-RPC 通知。 */
	CompletableFuture<Void> notifyClients(String method, Object params);

	/** 等待在途请求完成后关闭传输资源。 */
	CompletableFuture<Void> closeGracefully();

	/** 默认委托 {@link #closeGracefully()} 执行关闭。 */
	default void close() {
		closeGracefully();
	}

	/** 返回处理 Streamable HTTP 入站请求的 Netty 处理器。 */
	McpStreamableHttpRequestHandler getMcpRequestHandler();

}
