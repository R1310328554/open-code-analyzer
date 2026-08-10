// use-values.ts — Iteration 节点表单初始值：outputs 对象转数组供表单编辑。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { initialIterationValues } from '../../constant';
import { OutputObject } from './interface';

/** 将画布 outputs 对象转为表单用的 OutputArray。 */
function convertToArray(outputObject: OutputObject) {
  return Object.entries(outputObject).map(([key, value]) => ({
    name: key,
    ref: value.ref,
    type: value.type,
  }));
}

/** 空 form 返回 initialIterationValues；否则 outputs 转为数组。 */
export function useValues(node?: RAGFlowNodeType) {
  const values = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return { ...initialIterationValues, outputs: [] };
    }

    return { ...formData, outputs: convertToArray(formData.outputs) };
  }, [node?.data?.form]);

  return values;
}
