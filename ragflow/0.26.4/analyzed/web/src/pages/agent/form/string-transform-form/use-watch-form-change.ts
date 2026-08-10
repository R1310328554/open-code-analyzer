// use-watch-form-change.ts — StringTransform 表单监听：Merge 时 delimiters 包装为单元素数组写回。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import { StringTransformMethod } from '../../constant';
import useGraphStore from '../../store';

/** 脏变更时 Merge 方法将 delimiters 转为 [value] 后 updateNodeForm。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn<any>) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // 用户编辑触发的表单变更同步到画布
    if (id && form?.formState.isDirty) {
      values = form?.getValues();
      const nextValues: any = values;

      if (
        values.delimiters !== undefined &&
        values.method === StringTransformMethod.Merge
      ) {
        nextValues.delimiters = [values.delimiters];
      }

      updateNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
