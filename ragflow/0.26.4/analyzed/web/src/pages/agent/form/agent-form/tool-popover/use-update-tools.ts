// use-update-tools.ts — Agent 节点内置工具：读取、追加/切换与删除。

import { IAgentForm } from '@/interfaces/database/agent';
import { Operator } from '@/pages/agent/constant';
import { AgentFormContext } from '@/pages/agent/context';
import { useAgentToolInitialValues } from '@/pages/agent/hooks/use-agent-tool-initial-values';
import useGraphStore from '@/pages/agent/store';
import { get } from 'lodash';
import { useCallback, useContext, useMemo } from 'react';

/** 从 AgentFormContext 读取当前节点 form.tools 数组。 */
export function useGetNodeTools() {
  const node = useContext(AgentFormContext);

  return useMemo(() => {
    const tools: IAgentForm['tools'] = get(node, 'data.form.tools');
    return tools;
  }, [node]);
}

/** 追加 Retrieval 或切换其他工具开关，并生成 id/name/params。 */
export function useUpdateAgentNodeTools() {
  const { generateAgentToolName, generateAgentToolId, updateNodeForm } =
    useGraphStore((state) => state);
  const node = useContext(AgentFormContext)!;
  const tools = useGetNodeTools();
  const { initializeAgentToolValues } = useAgentToolInitialValues();

  const updateNodeTools = useCallback(
    (value: string) => {
      if (!node?.id) return;

      // Retrieval 允许多实例：直接追加
      if (value === Operator.Retrieval) {
        updateNodeForm(
          node.id,
          [
            ...tools,
            {
              component_name: value,
              name: generateAgentToolName(node.id, value),
              params: initializeAgentToolValues(value as Operator),
              id: generateAgentToolId(value),
            },
          ],
          ['tools'],
        );
      }
      // 其他工具：已存在则移除，否则追加
      else {
        updateNodeForm(
          node.id,
          tools.some((x) => x.component_name === value)
            ? tools.filter((x) => x.component_name !== value)
            : [
                ...tools,
                {
                  component_name: value,
                  name: value,
                  params: initializeAgentToolValues(value as Operator),
                  id: generateAgentToolId(value),
                },
              ],
          ['tools'],
        );
      }
    },
    [
      generateAgentToolName,
      generateAgentToolId,
      initializeAgentToolValues,
      node?.id,
      tools,
      updateNodeForm,
    ],
  );

  return { updateNodeTools };
}

/** 按 tool id 从节点 tools 列表删除一项。 */
export function useDeleteAgentNodeTools() {
  const { updateNodeForm } = useGraphStore((state) => state);
  const tools = useGetNodeTools();
  const node = useContext(AgentFormContext);

  const deleteNodeTool = useCallback(
    (toolId: string) => () => {
      const nextTools = tools.filter((x) => x.id !== toolId);

      if (node?.id) {
        updateNodeForm(node?.id, nextTools, ['tools']);
      }
    },
    [node?.id, tools, updateNodeForm],
  );

  return { deleteNodeTool };
}
