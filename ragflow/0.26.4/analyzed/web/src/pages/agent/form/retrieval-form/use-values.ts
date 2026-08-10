// use-values.ts — Retrieval 节点表单初始值：空节点用 initialRetrievalValues 默认配置。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { initialRetrievalValues } from '../../constant';

/** 从画布节点 form 读取 Retrieval 参数，无数据则返回 initialRetrievalValues。 */
export function useValues(node?: RAGFlowNodeType) {
  const defaultValues = useMemo(
    () => ({
      ...initialRetrievalValues,
    }),
    [],
  );

  const values = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return defaultValues;
    }

    return formData;
  }, [defaultValues, node?.data?.form]);

  return values;
}
