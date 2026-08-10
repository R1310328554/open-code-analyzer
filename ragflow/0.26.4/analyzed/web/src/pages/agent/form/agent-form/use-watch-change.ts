// use-watch-change.ts — react-hook-form 变更监听，脏数据同步回画布 DSL。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import { PromptRole } from '../../constant';
import useGraphStore from '../../store';

/** 表单 isDirty 时将 prompts 包装为数组并 updateNodeForm 写回节点。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn<any>) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // 用户编辑触发的脏表单变更同步到画布
    if (id && form?.formState.isDirty) {
      values = form?.getValues();
      const nextValues: any = {
        ...values,
        prompts: [{ role: PromptRole.User, content: values.prompts }],
      };

      updateNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
