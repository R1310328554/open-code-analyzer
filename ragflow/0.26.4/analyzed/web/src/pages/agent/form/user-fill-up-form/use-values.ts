// use-values.ts — UserFillUp 节点初始值：inputs 对象转为 Begin 风格数组供表单编辑。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { initialUserFillUpValues } from '../../constant';
import { buildBeginInputListFromObject } from '../begin-form/utils';

/** 从 node.data.form 读取配置，inputs 经 buildBeginInputListFromObject 适配 UI。 */
export function useValues(node?: RAGFlowNodeType) {
  const values = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return initialUserFillUpValues;
    }

    const inputs = buildBeginInputListFromObject(formData?.inputs);

    return { ...(formData || {}), inputs };
  }, [node?.data?.form]);

  return values;
}
