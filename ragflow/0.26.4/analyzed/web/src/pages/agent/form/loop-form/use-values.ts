// use-values.ts — Loop 节点表单初始值：从画布节点 form 推导并排除 outputs。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty, omit } from 'lodash';
import { useMemo } from 'react';

/** 合并 defaultValues 与节点 form，始终 omit outputs（由 watch 层动态生成）。 */
export function useFormValues(
  defaultValues: Record<string, any>,
  node?: RAGFlowNodeType,
) {
  const values = useMemo(() => {
    const formData = node?.data?.form;

    // 无节点数据时使用默认值（不含 outputs）
    if (isEmpty(formData)) {
      return omit(defaultValues, 'outputs');
    }

    return omit(formData, 'outputs');
  }, [defaultValues, node?.data?.form]);

  return values;
}
