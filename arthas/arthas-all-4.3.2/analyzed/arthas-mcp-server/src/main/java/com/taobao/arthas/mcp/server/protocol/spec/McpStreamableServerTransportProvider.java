/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 可流式 MCP 服务端传输提供者。
 * <p>
 * 扩展 {@link McpServerTransportProvider}，支持注入
 * {@link McpStreamableServerSession.Factory} 以管理长连接会话。
 */
public interface McpStreamableServerTransportProvider extends McpServerTransportProvider {


    /** 注册创建/初始化 Streamable 会话的工厂。 */
    void setSessionFactory(McpStreamableServerSession.Factory sessionFactory);

    /** 向所有 Streamable 会话广播 JSON-RPC 通知。 */
    CompletableFuture<Void> notifyClients(String method, Object params);

    /** 默认委托 {@link #closeGracefully()}。 */
    default void close() {
        this.closeGracefully();
    }

    /** 声明 Streamable HTTP 模式支持的协议版本（默认 2024-11-05）。 */
    default List<String> protocolVersions() {
        return Arrays.asList(ProtocolVersions.MCP_2024_11_05);
    }

}
