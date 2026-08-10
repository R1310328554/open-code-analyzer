// delete-node.ts — 删除 Agent 时收集其 Bottom/Tool 链上的全部下游节点与边。

import { Edge } from '@xyflow/react';
import { filterAllDownstreamAgentAndToolNodeIds } from './filter-downstream-nodes';

/** 递归收集并返回待删 Agent/Tool 节点 ID 及其关联边。
// Delete all downstream agent and tool operators of the current agent operator
/** 基于 filterAllDownstreamAgentAndToolNodeIds 汇总节点与 incident edges。 */
export function deleteAllDownstreamAgentsAndTool(
  nodeId: string,
  edges: Edge[],
) {
  const downstreamAgentAndToolNodeIds = filterAllDownstreamAgentAndToolNodeIds(
    edges,
    [nodeId],
  );

  const downstreamAgentAndToolEdges = downstreamAgentAndToolNodeIds.reduce<
    Edge[]
  >((pre, cur) => {
    const relatedEdges = edges.filter(
      (x) => x.source === cur || x.target === cur,
    );

    relatedEdges.forEach((x) => {
      if (!pre.some((y) => y.id !== x.id)) {
        pre.push(x);
      }
    });

    return pre;
  }, []);

  return {
    downstreamAgentAndToolNodeIds,
    downstreamAgentAndToolEdges,
  };
}
