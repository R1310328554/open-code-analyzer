// use-get-tools.ts — 读取 Agent 节点已绑定的 tools 与 MCP id 列表。

import { IAgentForm } from '@/interfaces/database/agent';
import { get } from 'lodash';
import { useContext, useMemo } from 'react';
import { AgentFormContext } from '../../context';

/** 返回当前节点 form.tools（默认空数组）。 */
export function useGetNodeTools() {
  const node = useContext(AgentFormContext);
  return get(node, 'data.form.tools', []) as IAgentForm['tools'];
}

/** 提取 tools 的 component_name 列表供 UI 展示/校验。 */
export function useGetAgentToolNames() {
  const node = useContext(AgentFormContext);

  const toolNames = useMemo(() => {
    const tools: IAgentForm['tools'] = get(node, 'data.form.tools', []);
    return tools.map((x) => x.component_name);
  }, [node]);

  return { toolNames };
}

/** 提取 mcp 条目的 mcp_id 列表。 */
export function useGetAgentMCPIds() {
  const node = useContext(AgentFormContext);

  const mcpIds = useMemo(() => {
    const ids: IAgentForm['mcp'] = get(node, 'data.form.mcp', []);
    return ids.map((x) => x.mcp_id);
  }, [node]);

  return { mcpIds };
}
