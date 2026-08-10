/**
 * mcp-server-service.ts — MCP 服务器 CRUD、导入/导出、连接测试及分页列表 API。
 */

import { IPaginationRequestBody } from '@/interfaces/request/base';
import api from '@/utils/api';
import request from '@/utils/request';

/** MCP 服务器对象式 API：get/create/update/delete/import/export/test。 */
const mcpServerService = {
  /** 预览模式获取 MCP 服务器配置。 */
  get: (params: { mcp_id: string }) =>
    request.get(api.getMcpServer(params.mcp_id), {
      params: { mode: 'preview' },
    }),
  /** 创建 MCP 服务器。 */
  create: (params?: Record<string, any>) =>
    request.post(api.createMcpServer, { data: params }),
  /** 更新 MCP 服务器配置。 */
  update: ({ mcp_id, ...params }: Record<string, any>) =>
    request.put(api.updateMcpServer(mcp_id), { data: params }),
  /** 删除 MCP 服务器。 */
  delete: ({ mcp_id }: { mcp_id: string }) =>
    request.delete(api.deleteMcpServer(mcp_id)),
  /** 从 JSON 导入 MCP 服务器配置。 */
  import: (params?: Record<string, any>) =>
    request.post(api.importMcpServer, { data: params }),
  /** 导出 MCP 服务器配置。 */
  export: ({ mcp_id }: { mcp_id: string }) =>
    request.get(api.exportMcpServer(mcp_id)),
  /** 测试 MCP 服务器连接（name 缺省为 preview）。 */
  test: (params: Record<string, any>) =>
    request.post(api.testMcpServer(params.name || 'preview'), { data: params }),
};

export default mcpServerService;

/** 分页列出 MCP 服务器（合并 query params 与 body）。 */
export const listMcpServers = (params?: IPaginationRequestBody, body?: any) =>
  request.get(api.listMcpServer, { params: { ...params, ...(body || {}) } });
