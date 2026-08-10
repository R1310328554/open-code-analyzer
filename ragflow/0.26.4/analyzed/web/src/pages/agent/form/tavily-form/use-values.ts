// use-values.ts — Tavily 搜索节点初始值：include/exclude_domains 转为对象数组供表单编辑。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { initialTavilyValues } from '../../constant';
import { convertToObjectArray } from '../../utils';

/** 从 node.data.form 读取 Tavily 配置，域名列表经 convertToObjectArray 适配 UI。 */
export function useValues(node?: RAGFlowNodeType) {
  const values = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return initialTavilyValues;
    }

    return {
      ...formData,
      include_domains: convertToObjectArray(formData.include_domains),
      exclude_domains: convertToObjectArray(formData.exclude_domains),
    };
  }, [node?.data?.form]);

  return values;
}
