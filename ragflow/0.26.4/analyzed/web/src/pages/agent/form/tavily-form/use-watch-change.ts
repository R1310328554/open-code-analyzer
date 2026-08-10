// use-watch-change.ts — Tavily 表单监听：域名对象数组还原为字符串数组并写回画布。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../../store';
import { convertToStringArray } from '../../utils';

/** 表单变更时 convertToStringArray 处理 include/exclude_domains 后 updateNodeForm。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn<any>) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // 用户编辑触发的表单变更同步到画布
    if (id) {
      values = form?.getValues();
      const nextValues: any = {
        ...values,
        include_domains: convertToStringArray(values.include_domains),
        exclude_domains: convertToStringArray(values.exclude_domains),
      };

      updateNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
