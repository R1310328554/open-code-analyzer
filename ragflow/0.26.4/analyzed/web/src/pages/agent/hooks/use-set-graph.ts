// use-set-graph.ts — 批量写入画布 nodes/edges 到 Zustand graph store。

import { IGraph } from '@/interfaces/database/agent';
import { useCallback } from 'react';
import useGraphStore from '../store';

/** 返回 setGraphInfo({ nodes, edges })，用于加载 Agent DSL 到画布。 */
export const useSetGraphInfo = () => {
  const { setEdges, setNodes } = useGraphStore((state) => state);
  const setGraphInfo = useCallback(
    ({ nodes = [], edges = [] }: IGraph) => {
      setNodes(nodes);
      setEdges(edges);
    },
    [setEdges, setNodes],
  );
  return setGraphInfo;
};
