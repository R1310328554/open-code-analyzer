// use-values.ts — Categorize 节点表单初始值：LLM 预设参数与 items 列表。

import { ModelVariableType } from '@/constants/knowledge';
import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty, isPlainObject } from 'lodash';
import { useMemo } from 'react';

/** 空节点默认：Precise parameter、历史窗口 1 与各 LLM 开关启用。 */
const defaultValues = {
  parameter: ModelVariableType.Precise,
  message_history_window_size: 1,
  temperatureEnabled: true,
  topPEnabled: true,
  presencePenaltyEnabled: true,
  frequencyPenaltyEnabled: true,
  maxTokensEnabled: true,
  items: [],
};

/** 从 node.data.form 读取 Categorize 表单值，空则返回 defaultValues。 */
export function useValues(node?: RAGFlowNodeType) {
  const values = useMemo(() => {
    const formData = node?.data?.form;
    if (isEmpty(formData)) {
      return defaultValues;
    }
    if (isPlainObject(formData)) {
      // const nextValues = {
      //   ...omit(formData, 'category_description'),
      //   items,
      // };

      return formData;
    }
  }, [node]);

  return values;
}
