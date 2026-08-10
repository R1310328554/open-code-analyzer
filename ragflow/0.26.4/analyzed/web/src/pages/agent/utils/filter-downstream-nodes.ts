// filter-downstream-nodes.ts — 沿边递归收集 Agent Bottom/Tool 等下游节点 ID。

import { Edge } from '@xyflow/react';
import { NodeHandleId } from '../constant';

/** 自 nodeIds 起按 predicate 过滤边，递归收集全部下游节点 ID。
// Get all downstream node ids
/** 通用下游 BFS：predicate 决定哪些出边参与遍历。 */
export function filterAllDownstreamNodeIds(
  edges: Edge[],
  nodeIds: string[],
  predicate: (edge: Edge) => boolean,
) {
  return nodeIds.reduce<string[]>((pre, nodeId) => {
    const currentEdges = edges.filter(
      (x) => x.source === nodeId && predicate(x),
    );

    const downstreamNodeIds: string[] = currentEdges.map((x) => x.target);

    const ids = downstreamNodeIds.concat(
      filterAllDownstreamNodeIds(edges, downstreamNodeIds, predicate),
    );

    ids.forEach((x) => {
      if (pre.every((y) => y !== x)) {
        pre.push(x);
      }
    });

    return pre;
  }, []);
}

/** 收集 Agent Bottom 与 Tool handle 链上的全部下游 Agent/Tool 节点。
// Get all downstream agent and tool operators of the current agent operator
/** AgentBottom 或 Tool 出边递归下游节点 ID 列表。 */
export function filterAllDownstreamAgentAndToolNodeIds(
  edges: Edge[],
  nodeIds: string[],
) {
  return filterAllDownstreamNodeIds(
    edges,
    nodeIds,
    (edge: Edge) =>
      edge.sourceHandle === NodeHandleId.AgentBottom ||
      edge.sourceHandle === NodeHandleId.Tool,
  );
}

/** 仅沿 AgentBottom 出边递归收集子 Agent 节点 ID。
// Get all downstream agent operators of the current agent operator
/** 递归收集 AgentBottom 链上的子 Agent 节点。 */
export function filterAllDownstreamAgentNodeIds(
  edges: Edge[],
  nodeIds: string[],
) {
  return filterAllDownstreamNodeIds(
    edges,
    nodeIds,
    (edge: Edge) => edge.sourceHandle === NodeHandleId.AgentBottom,
  );
}
/** 返回当前节点经 AgentBottom 直连的一层子 Agent ID。
// The direct child agent node of the current node
/** 非递归：仅取 source 为 nodeId 且 handle 为 AgentBottom 的目标。 */
export function filterDownstreamAgentNodeIds(edges: Edge[], nodeId?: string) {
  return edges
    .filter(
      (x) => x.source === nodeId && x.sourceHandle === NodeHandleId.AgentBottom,
    )
    .map((x) => x.target);
}
