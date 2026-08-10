// use-watch-change.ts — VariableAggregator 表单监听：groups 聚合为 outputs 并 replaceNodeForm。

import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../../store';
import { VariableAggregatorFormSchemaType } from './schema';

/** 脏变更时将各 group_name 映射为 outputs 键，type 写入输出 schema 后同步画布。 */
export function useWatchFormChange(
  id?: string,
  form?: UseFormReturn<VariableAggregatorFormSchemaType>,
) {
  const values = useWatch({ control: form?.control });
  const { replaceNodeForm } = useGraphStore((state) => state);

  useEffect(() => {
    if (id && form?.formState.isDirty) {
      const outputs = values.groups?.reduce(
        (pre, cur) => {
          if (cur.group_name) {
            pre[cur.group_name] = {
              type: cur.type,
            };
          }

          return pre;
        },
        {} as Record<string, Record<string, any>>,
      );

      replaceNodeForm(id, { ...values, outputs: outputs ?? {} });
    }
  }, [form?.formState.isDirty, id, replaceNodeForm, values]);
}
