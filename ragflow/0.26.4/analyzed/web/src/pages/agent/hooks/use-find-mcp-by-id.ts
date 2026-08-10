// use-find-mcp-by-id.ts — 在已加载的 MCP 服务列表中按 id 查找配置项。

import { useListMcpServer } from '@/hooks/use-mcp-request';

/** 基于 useListMcpServer 数据提供 findMcpById 查询函数。 */
export function useFindMcpById() {
  const { data } = useListMcpServer();

  /** 在 mcp_servers 数组中匹配 id，未找到返回 undefined。 */
  const findMcpById = (id: string) =>
    data.mcp_servers.find((item) => item.id === id);

  return {
    findMcpById,
  };
}
