// use-watch-change.ts — Agent LLM 节点「自由度/预设参数」切换时同步表单与画布状态。

import { settledModelVariableMap } from '@/constants/knowledge';
import { AgentFormContext } from '@/pages/agent/context';
import useGraphStore from '@/pages/agent/store';
import { setChatVariableEnabledFieldValuePage } from '@/utils/chat';
import { useCallback, useContext } from 'react';
import { useFormContext } from 'react-hook-form';

/** 监听 parameter 变更，批量写入 settledModelVariableMap 预设并更新节点表单。 */
export function useHandleFreedomChange(
  getFieldWithPrefix: (name: string) => string,
) {
  const form = useFormContext();
  const node = useContext(AgentFormContext);
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  /** 将 values 批量 setValue 到 react-hook-form（可选字段名前缀）。 */
  const setLLMParameters = useCallback(
    (values: Record<string, any>, withPrefix: boolean) => {
      for (const key in values) {
        if (Object.prototype.hasOwnProperty.call(values, key)) {
          const realKey = getFieldWithPrefix(key);
          const element = values[key as keyof typeof values];

          form.setValue(withPrefix ? realKey : key, element);
        }
      }
    },
    [form, getFieldWithPrefix],
  );

  /** 合并当前表单值与预设 map，同步 graph store 与聊天变量开关字段。 */
  const handleChange = useCallback(
    (parameter: string) => {
      const currentValues = { ...form.getValues() };
      const values =
        settledModelVariableMap[
          parameter as keyof typeof settledModelVariableMap
        ];

      const nextValues = { ...currentValues, ...values };

      if (node?.id) {
        updateNodeForm(node?.id, nextValues);
      }

      const variableCheckBoxFieldMap = setChatVariableEnabledFieldValuePage();

      setLLMParameters(values, true);
      setLLMParameters(variableCheckBoxFieldMap, false);
    },
    [form, node?.id, setLLMParameters, updateNodeForm],
  );

  return handleChange;
}
