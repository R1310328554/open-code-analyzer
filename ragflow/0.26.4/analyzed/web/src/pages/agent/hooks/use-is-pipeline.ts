// use-is-pipeline.ts — 根据 URL 查询参数判断当前画布是否为 Dataflow Pipeline 模式。

import { AgentCategory, AgentQuery } from '@/constants/agent';
import { useSearchParams } from 'react-router';

/** category=dataflow_canvas 时返回 true，用于区分 Agent 与 Pipeline 画布。 */
export function useIsPipeline() {
  const [queryParameters] = useSearchParams();

  return (
    queryParameters.get(AgentQuery.Category) === AgentCategory.DataflowCanvas
  );
}
