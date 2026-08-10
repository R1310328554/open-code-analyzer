// use-form-values.ts — 节点表单初始值：优先读取画布节点 data.form，空则回退 defaultValues。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';

/** 合并 defaultValues 与 node.data.form；form 为空时使用默认值。 */
export function useFormValues(
  defaultValues: Record<string, any>,
  node?: RAGFlowNodeType,
) {
  const values = useMemo(() => {
    // 从当前节点读取已持久化的表单快照
    const formData = node?.data?.form;

    // 节点尚无表单数据时回退到调用方默认值
    if (isEmpty(formData)) {
      return defaultValues;
    }

    return formData;
  }, [defaultValues, node?.data?.form]);

  return values;
}
