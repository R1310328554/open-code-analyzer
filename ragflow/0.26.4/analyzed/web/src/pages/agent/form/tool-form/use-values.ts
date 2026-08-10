// use-values.ts — Agent 子工具表单初始值：从 Agent 节点 tools 读取 params 或使用算子默认值。

import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { Operator } from '../../constant';
import { useAgentToolInitialValues } from '../../hooks/use-agent-tool-initial-values';
import useGraphStore from '../../store';

/** Tavily 等搜索工具的检索深度枚举。 */
export enum SearchDepth {
  Basic = 'basic',
  Advanced = 'advanced',
}

/** 搜索主题类型：新闻或通用。 */
export enum Topic {
  News = 'news',
  General = 'general',
}

/** 从 getAgentToolById 读取 params，空则用 initializeAgentToolValues 按算子初始化。 */
export function useValues() {
  const {
    clickedToolId,
    clickedNodeId,
    findUpstreamNodeById,
    getAgentToolById,
  } = useGraphStore();

  const { initializeAgentToolValues } = useAgentToolInitialValues();

  const values = useMemo(() => {
    const agentNode = findUpstreamNodeById(clickedNodeId);
    const tool = getAgentToolById(clickedToolId, agentNode!);
    const formData = tool?.params;

    if (isEmpty(formData)) {
      const defaultValues = initializeAgentToolValues(
        (tool?.component_name || clickedNodeId) as Operator,
      );

      return defaultValues;
    }

    return {
      ...formData,
    };
  }, [
    clickedNodeId,
    clickedToolId,
    findUpstreamNodeById,
    getAgentToolById,
    initializeAgentToolValues,
  ]);

  return values;
}
