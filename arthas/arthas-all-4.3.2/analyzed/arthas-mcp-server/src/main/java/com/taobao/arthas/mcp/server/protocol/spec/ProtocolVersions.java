package com.taobao.arthas.mcp.server.protocol.spec;

/**
 * MCP 协议版本字符串常量。
 * <p>
 * 各传输实现通过 {@code protocolVersions()} 声明支持的版本子集，
 * 客户端在 HTTP 头 {@link HttpHeaders#PROTOCOL_VERSION} 中协商。
 */
public interface ProtocolVersions {

	/** MCP 2024-11-05 协议版本（Streamable HTTP 早期规范）。
	 * @see <a href="https://modelcontextprotocol.io/specification/2024-11-05">规范文档</a>
	 */
	String MCP_2024_11_05 = "2024-11-05";

	/** MCP 2025-03-26 协议版本。
	 * @see <a href="https://modelcontextprotocol.io/specification/2025-03-26">规范文档</a>
	 */
	String MCP_2025_03_26 = "2025-03-26";

	/** MCP 2025-06-18 协议版本。
	 * @see <a href="https://modelcontextprotocol.io/specification/2025-06-18">规范文档</a>
	 */
	String MCP_2025_06_18 = "2025-06-18";

	/** MCP 2025-11-25 协议版本（{@link McpSchema#LATEST_PROTOCOL_VERSION} 默认值）。
	 * @see <a href="https://modelcontextprotocol.io/specification/2025-11-25">规范文档</a>
	 */
	String MCP_2025_11_25 = "2025-11-25";

}
