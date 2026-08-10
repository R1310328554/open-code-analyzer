// use-values.ts — MCP 工具表单初始值：从上游 Agent 节点的 mcp 列表读取已选 tools 键名。

import useGraphStore from '@/pages/agent/store';
import { getAgentNodeMCP } from '@/pages/agent/utils';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';

/** 根据 clickedToolId 在上游 Agent 的 MCP 配置中查找 tools，返回 items 为工具名数组。 */
export function useValues() {
  const { clickedToolId, clickedNodeId, findUpstreamNodeById } = useGraphStore(
    (state) => state,
  );

  const values = useMemo(() => {
    const agentNode = findUpstreamNodeById(clickedNodeId);
    const mcpList = getAgentNodeMCP(agentNode);

    const formData =
      mcpList.find((x) => x.mcp_id === clickedToolId)?.tools || {};

    // 无已选工具时返回空 items
    if (isEmpty(formData)) {
      return { items: [] };
    }

    return { items: Object.keys(formData) };
  }, [clickedNodeId, clickedToolId, findUpstreamNodeById]);

  return values;
}
