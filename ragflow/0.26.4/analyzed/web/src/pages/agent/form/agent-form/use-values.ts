// use-values.ts — Agent 表单初始值：合并默认模型并排除非表单字段。

import { useFetchDefaultModelDictionary } from '@/hooks/use-llm-request';
import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { get, isEmpty, omit } from 'lodash';
import { useMemo } from 'react';
import { initialAgentValues } from '../../constant';

// 表单不含 mcp/tools/outputs：useWatch 同步时需 omit，避免覆盖画布侧直接维护的数据。
/** 从表单值中剔除 mcp、tools、outputs 字段。 */
function omitToolsAndMcp(values: Record<string, any>) {
  return omit(values, ['mcp', 'tools', 'outputs']);
}

/** 计算 Agent 节点表单 defaultValues：默认 LLM、prompts 字符串化等。 */
export function useValues(node?: RAGFlowNodeType) {
  const defaultModelDictionary = useFetchDefaultModelDictionary();

  const defaultValues = useMemo(
    () => ({
      ...omitToolsAndMcp(initialAgentValues),
      llm_id: defaultModelDictionary.llm_id,
      prompts: '',
    }),
    [defaultModelDictionary],
  );

  const values = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return defaultValues;
    }

    return {
      ...omitToolsAndMcp(formData),
      prompts: get(formData, 'prompts.0.content', ''),
    };
  }, [defaultValues, node?.data?.form]);

  return values;
}
