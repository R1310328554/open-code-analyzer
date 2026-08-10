// use-filter-child-node-ids.ts — 从画布 store 读取节点并过滤指定父节点的子节点 ID。

import { filterChildNodeIds } from '@/utils/canvas-util';
import useGraphStore from '../store';

/** 调用 canvas-util.filterChildNodeIds，无子节点时返回空数组。 */
export function useFilterChildNodeIds(nodeId?: string) {
  const nodes = useGraphStore((state) => state.nodes);

  const childNodeIds = filterChildNodeIds(nodes, nodeId);

  return childNodeIds ?? [];
}
