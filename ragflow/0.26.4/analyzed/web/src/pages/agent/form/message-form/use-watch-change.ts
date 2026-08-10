// use-watch-change.ts — Message 表单监听：content 对象数组还原为 DSL 字符串数组并写回画布。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../../store';
import { convertToStringArray } from '../../utils';

/** 表单脏变更时 convertToStringArray(content) 后 updateNodeForm 同步节点。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // 用户编辑触发的表单变更同步到画布
    if (id && form?.formState.isDirty) {
      values = form?.getValues();
      let nextValues: any = values;

      nextValues = {
        ...values,
        content: convertToStringArray(values.content),
      };

      updateNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
