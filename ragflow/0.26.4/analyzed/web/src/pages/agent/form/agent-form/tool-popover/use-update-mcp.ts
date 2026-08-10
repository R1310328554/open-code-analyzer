// use-update-mcp.ts — Agent 节点 MCP 绑定：读取、更新与删除。

import { useListMcpServer } from '@/hooks/use-mcp-request';
import { IAgentForm } from '@/interfaces/database/agent';
import { AgentFormContext } from '@/pages/agent/context';
import useGraphStore from '@/pages/agent/store';
import { get } from 'lodash';
import { useCallback, useContext, useMemo } from 'react';

/** 从 AgentFormContext 读取当前节点 form.mcp 配置。 */
export function useGetNodeMCP() {
  const node = useContext(AgentFormContext);

  return useMemo(() => {
    const mcp: IAgentForm['mcp'] = get(node, 'data.form.mcp');
    return mcp;
  }, [node]);
}

/** 按选中 mcp_id 列表更新节点 MCP，保留已有项或初始化 tools。 */
export function useUpdateAgentNodeMCP() {
  const { updateNodeForm } = useGraphStore((state) => state);
  const node = useContext(AgentFormContext);
  const mcpList = useGetNodeMCP();
  const { data } = useListMcpServer();
  const mcpServers = data.mcp_servers;

  /** 根据 MCP 服务器 id 查找其 tools 定义。 */
  const findMcpTools = useCallback(
    (mcpId: string) => {
      const mcp = mcpServers.find((x) => x.id === mcpId);
      return mcp?.variables.tools;
    },
    [mcpServers],
  );

  /** 合并/新建 mcp 条目并写回画布节点 form.mcp。 */
  const updateNodeMCP = useCallback(
    (value: string[]) => {
      if (node?.id) {
        const nextValue = value.reduce<IAgentForm['mcp']>((pre, cur) => {
          const mcp = mcpList.find((x) => x.mcp_id === cur);
          const tools = findMcpTools(cur);
          if (mcp) {
            pre.push(mcp);
          } else if (tools) {
            pre.push({
              mcp_id: cur,
              tools: {},
            });
          }
          return pre;
        }, []);

        updateNodeForm(node?.id, nextValue, ['mcp']);
      }
    },
    [node?.id, updateNodeForm, mcpList, findMcpTools],
  );

  return { updateNodeMCP };
}

/** 按 mcp_id 从节点移除一条 MCP 绑定。 */
export function useDeleteAgentNodeMCP() {
  const { updateNodeForm } = useGraphStore((state) => state);
  const mcpList = useGetNodeMCP();
  const node = useContext(AgentFormContext);

  const deleteNodeMCP = useCallback(
    (value: string) => () => {
      const nextMCP = mcpList.filter((x) => x.mcp_id !== value);
      if (node?.id) {
        updateNodeForm(node?.id, nextMCP, ['mcp']);
      }
    },
    [node?.id, mcpList, updateNodeForm],
  );

  return { deleteNodeMCP };
}
