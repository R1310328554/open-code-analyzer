// use-watch-change.ts — Note 节点表单监听：字段与名称变更同步回画布 graph store。

import useGraphStore from '@/pages/agent/store';
import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';

/** 监听 Note 表单全量字段，变更时 updateNodeForm 写回画布节点。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn<any>) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // 用户编辑触发的表单变更同步到画布
    if (id) {
      values = form?.getValues() || {};
      const nextValues: any = values;

      updateNodeForm(id, nextValues);
    }
  }, [id, updateNodeForm, values]);
}

/** 监听 Note 节点 name 字段，变更时 updateNodeName 同步画布显示名。 */
export function useWatchNameFormChange(id?: string, form?: UseFormReturn<any>) {
  const values = useWatch({ control: form?.control });
  const updateNodeName = useGraphStore((state) => state.updateNodeName);

  useEffect(() => {
    // Manually triggered form updates are synchronized to the canvas
    if (id) {
      updateNodeName(id, values.name);
    }
  }, [id, updateNodeName, values]);
}
