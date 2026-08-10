// use-values.ts — StringTransform 节点初始值：Merge 模式下 delimiters 单值/数组形态转换。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import {
  initialStringTransformValues,
  StringTransformMethod,
} from '../../constant';

/** Merge 方法取 delimiters[0] 为单值，其余方法保持数组形态。 */
function transferDelimiters(formData: typeof initialStringTransformValues) {
  return formData.method === StringTransformMethod.Merge
    ? formData.delimiters[0]
    : formData.delimiters;
}

/** 合并节点 form 与默认值，delimiters 经 transferDelimiters 适配当前 method。 */
export function useValues(node?: RAGFlowNodeType) {
  const values = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return {
        ...initialStringTransformValues,
        delimiters: transferDelimiters(formData),
      };
    }

    return {
      ...formData,
      delimiters: transferDelimiters(formData),
    };
  }, [node?.data?.form]);

  return values;
}
