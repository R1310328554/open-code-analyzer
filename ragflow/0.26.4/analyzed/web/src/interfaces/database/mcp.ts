// mcp.ts — MCP 服务器列表、工具 schema 与导出配置类型。

/** 后端返回的 MCP 服务器记录：URL、类型与 variables（含 tools）。 */
export interface IMcpServer {
  create_date: string;
  description: null;
  id: string;
  name: string;
  server_type: string;
  update_date: string;
  url: string;
  variables: Record<string, any> & { tools?: IMCPToolObject };
}

/** 工具名 → 工具定义（不含 name 字段）的映射。 */
export type IMCPToolObject = Record<string, Omit<IMCPTool, 'name'>>;

/** 工具名 → 完整 IMCPTool 记录。 */
export type IMCPToolRecord = Record<string, IMCPTool>;

/** MCP 服务器分页列表响应。 */
export interface IMcpServerListResponse {
  mcp_servers: IMcpServer[];
  total: number;
}

/** MCP 工具元数据：描述、JSON Schema 输入与启用开关。 */
export interface IMCPTool {
  annotations: null;
  description: string;
  enabled: boolean;
  inputSchema: InputSchema;
  name: string;
}

/** MCP 工具 inputSchema：JSON Schema 子集。 */
interface InputSchema {
  properties: Properties;
  required: string[];
  title: string;
  type: string;
}

/** inputSchema.properties 容器（示例含 symbol 参数）。 */
interface Properties {
  symbol: ISymbol;
}

/** 单个 schema 属性：title 与 type。 */
interface ISymbol {
  title: string;
  type: string;
}

/** 导出格式根对象：mcpServers 字典（兼容 Claude Desktop 等）。 */
export interface IExportedMcpServers {
  mcpServers: Record<string, IExportedMcpServer>;
}

/** 单条导出 MCP 配置：授权 token、URL 与 tool_configuration。 */
export interface IExportedMcpServer {
  authorization_token: string;
  name: string;
  tool_configuration: Record<string, any>;
  type: string;
  url: string;
}
