// use-show-structured-output-dialog.ts — Agent 结构化输出 JSON Schema 弹窗与开关。

import { JSONSchema } from '@/components/jsonjoy-builder';
import { AgentStructuredOutputField } from '@/constants/agent';
import { useSetModalState } from '@/hooks/common-hooks';
import { useCallback } from 'react';
import { initialAgentValues } from '../../constant';
import useGraphStore from '../../store';

/** 管理结构化输出编辑弹窗，确认后将 JSONSchema 写入节点 outputs。 */
export function useShowStructuredOutputDialog(nodeId?: string) {
  const {
    visible: structuredOutputDialogVisible,
    showModal: showStructuredOutputDialog,
    hideModal: hideStructuredOutputDialog,
  } = useSetModalState();
  const { updateNodeForm } = useGraphStore((state) => state);

  const handleStructuredOutputDialogOk = useCallback(
    (values: JSONSchema) => {
      // 将编辑结果同步到画布节点 outputs.structured
      if (nodeId) {
        updateNodeForm(nodeId, values, ['outputs', AgentStructuredOutputField]);
      }
      hideStructuredOutputDialog();
    },
    [hideStructuredOutputDialog, nodeId, updateNodeForm],
  );

  return {
    structuredOutputDialogVisible,
    showStructuredOutputDialog,
    hideStructuredOutputDialog,
    handleStructuredOutputDialogOk,
  };
}

/** 切换是否启用结构化输出：开则初始化字段，关则恢复默认 outputs。 */
export function useHandleShowStructuredOutput(nodeId?: string) {
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  const handleShowStructuredOutput = useCallback(
    (val: boolean) => {
      if (nodeId) {
        if (val) {
          updateNodeForm(nodeId, {}, ['outputs', AgentStructuredOutputField]);
        } else {
          updateNodeForm(nodeId, initialAgentValues.outputs, ['outputs']);
        }
      }
    },
    [nodeId, updateNodeForm],
  );

  return {
    handleShowStructuredOutput,
  };
}
