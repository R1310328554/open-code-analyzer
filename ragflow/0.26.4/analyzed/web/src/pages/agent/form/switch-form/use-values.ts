// use-values.ts — Switch 节点表单初始值：空节点返回 initialSwitchValues。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { initialSwitchValues } from '../../constant';

/** 从 node.data.form 读取 Switch 分支条件配置，无数据用 initialSwitchValues。 */
export function useValues(node?: RAGFlowNodeType) {
  const values = useMemo(() => {
    const formData = node?.data?.form;
    if (isEmpty(formData)) {
      return initialSwitchValues;
    }

    return formData;
  }, [node]);

  return values;
}
