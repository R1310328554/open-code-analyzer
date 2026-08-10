// use-watch-change.ts — MCP 表单监听：勾选变更后 pick 服务端 tools 并写回 Agent 节点 mcp 字段。

import { useGetMcpServer } from '@/hooks/use-mcp-request';
import useGraphStore from '@/pages/agent/store';
import { getAgentNodeMCP } from '@/pages/agent/utils';
import { pick } from 'lodash';
import { useEffect, useMemo } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';

/** 监听 items 勾选，从 MCP 服务端 variables.tools 中 pick 选中项并 updateNodeForm。 */
export function useWatchFormChange(form?: UseFormReturn<any>) {
  let values = useWatch({ control: form?.control });
  const { clickedToolId, clickedNodeId, findUpstreamNodeById, updateNodeForm } =
    useGraphStore((state) => state);
  const { data } = useGetMcpServer(clickedToolId);

  const nextMCPTools = useMemo(() => {
    const mcpTools = data.variables?.tools || [];
    values = form?.getValues();

    return pick(mcpTools, values.items);
  }, [values, data?.variables]);

  useEffect(() => {
    const agentNode = findUpstreamNodeById(clickedNodeId);
    // 用户勾选变更同步到画布 Agent 节点的 mcp 配置
    if (agentNode) {
      const agentNodeId = agentNode?.id;
      const mcpList = getAgentNodeMCP(agentNode);

      const nextMCP = mcpList.map((x) => {
        if (x.mcp_id === clickedToolId) {
          return {
            ...x,
            tools: nextMCPTools,
          };
        }
        return x;
      });

      updateNodeForm(agentNodeId, nextMCP, ['mcp']);
    }
  }, [
    clickedNodeId,
    clickedToolId,
    findUpstreamNodeById,
    nextMCPTools,
    updateNodeForm,
  ]);
}
