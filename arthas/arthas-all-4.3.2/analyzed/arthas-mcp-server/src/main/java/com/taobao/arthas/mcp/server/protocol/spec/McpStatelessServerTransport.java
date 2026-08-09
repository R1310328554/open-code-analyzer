/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import com.taobao.arthas.mcp.server.protocol.server.McpStatelessServerHandler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 无状态 MCP 服务端传输层接口。
 * <p>
 * 每个 HTTP 请求独立处理，不维护长连接会话；
 * 通过 {@link #setMcpHandler} 绑定 {@link McpStatelessServerHandler}。
 */
public interface McpStatelessServerTransport {

	/** 注册处理无状态 MCP 消息的处理器。 */
	void setMcpHandler(McpStatelessServerHandler mcpHandler);

	/** 默认委托 {@link #closeGracefully()}。 */
	default void close() {
		this.closeGracefully();
	}

	/** 优雅关闭底层监听与连接池。 */
	CompletableFuture<Void> closeGracefully();

    /** 声明本传输支持的 MCP 协议版本列表（无状态模式）。 */
    default List<String> protocolVersions() {
        return Arrays.asList(ProtocolVersions.MCP_2025_03_26, ProtocolVersions.MCP_2025_06_18,
                ProtocolVersions.MCP_2025_11_25);
    }

}
