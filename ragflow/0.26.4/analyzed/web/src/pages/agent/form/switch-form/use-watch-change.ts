// use-watch-change.ts — Switch 表单监听：conditions 浅拷贝后同步到画布（避免 useFieldArray 引用共享）。

import { ISwitchCondition } from '@/interfaces/database/agent';
import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../../store';

/** 监听表单变更，conditions 逐项浅拷贝后 updateNodeForm 写回 Switch 节点。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // 用户编辑触发的表单变更同步到画布
    console.log('🚀 ~ useWatchFormChange ~ values:', form?.formState.isDirty);
    if (id) {
      values = form?.getValues() || {};
      const nextValues: any = {
        ...values,
        // useFieldArray 不更新数组引用，浅拷贝 conditions 避免画布与表单共享引用
        conditions:
          values?.conditions?.map((x: ISwitchCondition) => ({ ...x })) ?? [], // Changing the form value with useFieldArray does not change the array reference
      };

      updateNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
