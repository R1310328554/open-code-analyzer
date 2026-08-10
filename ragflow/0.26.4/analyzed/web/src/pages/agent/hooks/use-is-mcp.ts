// use-is-mcp.ts — 判断当前 Tool 算子是否为 MCP 外部工具（非内置 Operator 枚举）。

import { Operator } from '../constant';
import useGraphStore from '../store';

/** 当算子为 Tool 且 component_name 不在内置 Operator 列表时视为 MCP 工具。 */
export function useIsMcp(operatorName: Operator) {
  const { clickedToolId, getAgentToolById } = useGraphStore();

  // 根据当前选中的 Agent 子工具 ID 解析 component_name
  const { component_name: toolName } = getAgentToolById(clickedToolId) ?? {};

  return (
    operatorName === Operator.Tool &&
    Object.values(Operator).every((x) => x !== toolName)
  );
}
