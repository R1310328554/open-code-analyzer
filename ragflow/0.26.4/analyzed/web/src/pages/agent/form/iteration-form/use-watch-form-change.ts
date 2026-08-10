// use-watch-form-change.ts — Iteration 表单脏数据同步：outputs 数组转对象写回画布。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../../store';
import { OutputArray, OutputObject } from './interface';

/** 将表单 outputs 数组还原为画布 DSL 用的 OutputObject。 */
export function transferToObject(list: OutputArray) {
  return list.reduce<OutputObject>((pre, cur) => {
    pre[cur.name] = { ref: cur.ref, type: cur.type };
    return pre;
  }, {});
}

/** 表单 isDirty 时将 outputs 转为对象并 updateNodeForm。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // 用户编辑触发的脏表单变更同步到画布
    if (id && form?.formState.isDirty) {
      values = form?.getValues();
      console.log('🚀 ~ useEffect ~ values:', values);
      const nextValues: any = {
        ...values,
        outputs: transferToObject(values.outputs),
      };

      updateNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
