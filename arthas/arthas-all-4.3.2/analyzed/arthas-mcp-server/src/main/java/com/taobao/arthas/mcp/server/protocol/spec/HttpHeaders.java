/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

/**
 * MCP Streamable HTTP 传输层使用的标准 HTTP 头常量。
 */
public interface HttpHeaders {

	/** 标识单个 MCP 会话，客户端在后续请求中回传以维持会话。 */
	String MCP_SESSION_ID = "mcp-session-id";

	/** SSE 流内事件标识，用于断线后从 Last-Event-ID 续传。 */
	String LAST_EVENT_ID = "last-event-id";

	/** 客户端声明的 MCP 协议版本，服务端据此协商能力。 */
	String PROTOCOL_VERSION = "MCP-Protocol-Version";

}
