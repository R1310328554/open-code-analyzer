// use-watch-change.ts — UserFillUp 表单监听：inputs 数组转对象并同步 outputs 到画布。

import { omit } from 'lodash';
import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import { BeginQuery } from '../../interface';
import useGraphStore from '../../store';

/** 将表单 inputs 数组（含 key）转为 DSL 所需的 Record<key, Omit<BeginQuery,'key'>>。 */
function transferInputsArrayToObject(inputs: BeginQuery[] = []) {
  return inputs.reduce<Record<string, Omit<BeginQuery, 'key'>>>((pre, cur) => {
    pre[cur.key] = omit(cur, 'key');

    return pre;
  }, {});
}

/** 表单脏变更时 inputs 转对象，outputs 与 inputs 一致后 updateNodeForm。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    // TODO: This should only be executed when the form changes
    if (id) {
      values = form?.getValues() || {};

      const inputs = transferInputsArrayToObject(values.inputs);

      const nextValues = {
        ...values,
        inputs,
        outputs: inputs,
      };

      updateNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
