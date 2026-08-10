// mcp-server.ts — MCP 服务器连接配置：传输类型、变量与请求头。

/** MCP 传输协议：SSE 或 Streamable HTTP。 */
export enum McpServerType {
    Sse = 'sse',
    StreamableHttp = 'streamable-http',
}

/** MCP 服务器环境变量占位：key 与展示名。 */
export interface IMcpServerVariable {
    key: string;
    name: string;
}

/** 前端编辑/连接用 MCP 服务器完整配置（含 headers Map）。 */
export interface IMcpServerInfo {
    id: string;
    name: string;
    url: string;
    server_type: McpServerType;
    description?: string;
    variables?: IMcpServerVariable[];
    headers: Map<string, string>;
}
