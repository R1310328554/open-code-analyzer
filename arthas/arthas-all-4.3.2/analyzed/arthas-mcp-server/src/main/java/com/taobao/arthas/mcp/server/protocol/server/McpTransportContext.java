/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server;

/**
 * MCP 传输层上下文，在单次 HTTP 请求处理过程中携带键值元数据。
 * <p>
 * 典型用途：存放认证主体、User ID 及自定义扩展字段，供工具/资源处理器读取。
 */
public interface McpTransportContext {

	/** Channel 属性中存放本上下文实例的键名。 */
	String KEY = "MCP_TRANSPORT_CONTEXT";

	/** 空上下文单例，extractor 未填充额外字段时使用。 */
	McpTransportContext EMPTY = new DefaultMcpTransportContext();

	Object get(String key);

	void put(String key, Object value);

	McpTransportContext copy();

}
