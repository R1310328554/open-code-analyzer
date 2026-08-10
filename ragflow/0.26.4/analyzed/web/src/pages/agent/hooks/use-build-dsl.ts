// use-build-dsl.ts — 将画布 nodes/edges 与已有 DSL 合并为可保存的 Agent DSL 结构。

import { useFetchAgent } from '@/hooks/use-agent-request';
import {
  GlobalVariableType,
  RAGFlowNodeType,
} from '@/interfaces/database/agent';
import { useCallback } from 'react';
import useGraphStore from '../store';
import { graphToDsl } from '../utils/dsl-bridge';

/** 读取 store 与 useFetchAgent 的 dsl，经 graphToDsl 转为后端格式，可传入 globalVariables。 */
export const useBuildDslData = () => {
  const { data } = useFetchAgent();
  const { nodes, edges } = useGraphStore((state) => state);

  /** currentNodes 可选覆盖 store nodes，otherParam 可附加全局变量。 */
  const buildDslData = useCallback(
    (
      currentNodes?: RAGFlowNodeType[],
      otherParam?: { globalVariables: Record<string, GlobalVariableType> },
    ) => {
      return graphToDsl(
        currentNodes ?? nodes,
        edges,
        data?.dsl ?? {},
        otherParam?.globalVariables,
      );
    },
    [data?.dsl, edges, nodes],
  );

  return { buildDslData };
};
