/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import io.netty.channel.Channel;

/**
 * 服务端传输层接口，在 {@link McpTransport} 基础上暴露底层 Netty {@link Channel}。
 * <p>
 * 便于在 HTTP 处理器中访问连接属性、远程地址或执行通道级操作。
 *
 * @author Yeaury
 */
public interface McpServerTransport extends McpTransport {

    /** 返回承载当前 MCP 消息的 Netty 通道。 */
    Channel getChannel();
}
