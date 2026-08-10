// mcp.ts — MCP 服务器连通性测试与批量导入请求体。

import { IExportedMcpServer } from '@/interfaces/database/mcp';

/** 测试 MCP 连接：server_type、url、headers、variables 与 timeout。 */
export interface ITestMcpRequestBody {
  server_type: string;
  url: string;
  headers?: Record<string, any>;
  variables?: Record<string, any>;
  timeout?: number;
}

/** 从导出 JSON 批量导入 MCP：mcpServers 字典（type/url/token）。 */
export interface IImportMcpServersRequestBody {
  mcpServers: Record<
    string,
    Pick<IExportedMcpServer, 'type' | 'url' | 'authorization_token'>
  >;
}
