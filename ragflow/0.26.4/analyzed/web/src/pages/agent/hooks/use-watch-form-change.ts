// use-watch-form-change.ts — Agent 画布通用表单监听：表单脏数据同步到节点 form。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../store';

/** 监听 react-hook-form 变更，在 isDirty 时将整表或局部值写回画布 store。 */
export function useWatchFormChange(
  id?: string,
  form?: UseFormReturn<any>,
  enableReplacement = false,
) {
  let values = useWatch({ control: form?.control });
  const { updateNodeForm, replaceNodeForm } = useGraphStore((state) => state);

  useEffect(() => {
    // 表单手动编辑后，将最新 values 同步到画布节点 form
    // Manually triggered form updates are synchronized to the canvas
    if (id) {
      values = form?.getValues() || {};
      const nextValues: any = values;

      // enableReplacement 为 true 时整表替换，否则增量 merge
      (enableReplacement ? replaceNodeForm : updateNodeForm)(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
