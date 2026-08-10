// use-watch-change.ts — Categorize 表单监听：items 浅拷贝后同步到画布 graph store。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../../store';

/** 监听表单变更，updateNodeForm 写回 Categorize 节点（items 拷贝避免引用共享）。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn<any>) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // 用户编辑触发的表单变更同步到画布
    if (id) {
      values = form?.getValues();

      updateNodeForm(id, { ...values, items: values.items?.slice() || [] });
    }
  }, [id, updateNodeForm, values]);
}
