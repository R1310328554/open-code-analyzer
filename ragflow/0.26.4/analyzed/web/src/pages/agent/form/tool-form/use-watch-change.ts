// use-watch-change.ts — Agent 子工具表单监听：脏变更时将 params 写回 Agent 节点对应 tool。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../../store';

/** 表单 isDirty 时 updateAgentToolById 同步 params 到画布。 */
export function useWatchFormChange(form?: UseFormReturn<any>) {
  const values = useWatch({ control: form?.control });

  const {
    clickedToolId,
    clickedNodeId,
    findUpstreamNodeById,
    getAgentToolById,
    updateAgentToolById,
    updateNodeForm,
  } = useGraphStore();

  useEffect(() => {
    const agentNode = findUpstreamNodeById(clickedNodeId);
    // 用户编辑触发的表单变更同步到画布
    if (agentNode && form?.formState.isDirty) {
      updateAgentToolById(agentNode, clickedToolId, {
        params: {
          ...(values ?? {}),
        },
      });
    }
  }, [
    clickedNodeId,
    clickedToolId,
    findUpstreamNodeById,
    form,
    form?.formState.isDirty,
    getAgentToolById,
    updateAgentToolById,
    updateNodeForm,
    values,
  ]);
}
