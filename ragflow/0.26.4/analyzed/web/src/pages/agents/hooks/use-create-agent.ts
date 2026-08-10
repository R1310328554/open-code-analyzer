// use-create-agent.ts — 创建 Agent 或 Dataflow：弹窗提交与 initialEmptyDsl 初始化。

import { AgentCategory } from '@/constants/agent';
import { useSetModalState } from '@/hooks/common-hooks';
import { useSetAgent } from '@/hooks/use-agent-request';

import { initialEmptyDsl } from '@/pages/agent/utils/dsl-bridge';
import { useCallback } from 'react';
import { FlowType } from '../constant';
import { FormSchemaType } from '../create-agent-form';

/** 封装创建弹窗显隐、loading 与 handleCreateAgentOrPipeline 提交逻辑。 */
export function useCreateAgentOrPipeline() {
  const { loading, setAgent } = useSetAgent();
  const {
    visible: creatingVisible,
    hideModal: hideCreatingModal,
    showModal: showCreatingModal,
  } = useSetModalState();

  /** 按 FlowType 选择空 DSL 与 canvas_category 并调用 setAgent API。 */
  const handleCreateAgentOrPipeline = useCallback(
    async (data: FormSchemaType) => {
      // Agent 用 AgentCanvas，Flow 用 DataflowCanvas
      const isAgent = data.type === FlowType.Agent;
      const ret = await setAgent({
        title: data.name,
        dsl: initialEmptyDsl(isAgent),
        canvas_category: isAgent
          ? AgentCategory.AgentCanvas
          : AgentCategory.DataflowCanvas,
      });

      // 创建成功后关闭弹窗
      if (ret.code === 0) {
        hideCreatingModal();
      }
    },
    [hideCreatingModal, setAgent],
  );

  return {
    loading: loading,
    creatingVisible,
    hideCreatingModal,
    showCreatingModal,
    handleCreateAgentOrPipeline,
  };
}
