// use-values.ts — Message 节点表单初始值：content 由字符串数组转为对象数组供表单编辑。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { initialMessageValues } from '../../constant';
import { convertToObjectArray } from '../../utils';

/** 从 node.data.form 读取 Message 配置，content 经 convertToObjectArray 适配 UI。 */
export function useValues(node?: RAGFlowNodeType) {
  const values = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return initialMessageValues;
    }

    return {
      ...formData,
      content: convertToObjectArray(formData.content),
    };
  }, [node]);

  return values;
}
