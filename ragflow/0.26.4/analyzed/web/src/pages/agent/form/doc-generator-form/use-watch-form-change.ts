// use-watch-form-change.ts — DocGenerator 表单 watch：实时同步到画布节点 form。

import { useEffect } from 'react';
import { UseFormReturn } from 'react-hook-form';
import useGraphStore from '../../store';

/** 订阅 react-hook-form 变更，通过 updateNodeForm 写回指定 nodeId。 */
export const useWatchFormChange = (
  nodeId: string | undefined,
  form: UseFormReturn<any>,
) => {
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    const { unsubscribe } = form.watch((value) => {
      if (nodeId) {
        updateNodeForm(nodeId, value);
      }
    });
    return () => unsubscribe();
  }, [form, nodeId, updateNodeForm]);
};
