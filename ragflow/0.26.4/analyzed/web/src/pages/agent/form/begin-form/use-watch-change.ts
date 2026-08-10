// use-watch-change.ts — Begin 表单监听：inputs 转对象、Webhook schema 推导 outputs 并写回画布。

import { isEmpty, omit } from 'lodash';
import { useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import { AgentDialogueMode } from '../../constant';
import { BeginQuery } from '../../interface';
import useGraphStore from '../../store';
import { BeginFormSchemaType } from './schema';

/** 将 inputs 数组按 key 聚合为 DSL 所需的 Record（去掉 key 字段）。 */
export function transferInputsArrayToObject(inputs: BeginQuery[] = []) {
  return inputs.reduce<Record<string, Omit<BeginQuery, 'key'>>>((pre, cur) => {
    pre[cur.key] = omit(cur, 'key');

    return pre;
  }, {});
}

/** Webhook 模式下把 request schema 各段展开为 outputs 键（如 body.xxx）。 */
function transformRequestSchemaToOutput(schema: BeginFormSchemaType['schema']) {
  const outputs: Record<string, any> = {};

  Object.entries(schema || {}).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      for (const cur of value) {
        outputs[`${key}.${cur.key}`] = { type: cur.type };
      }
    }
  });

  return outputs;
}

/** 表单脏变更时同步节点 form：inputs 对象化，Webhook 时附加 outputs。 */
export function useWatchFormChange(id?: string, form?: UseFormReturn) {
  let values = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  useEffect(() => {
    if (id) {
      values = form?.getValues() || {};

      let outputs: Record<string, any> = {};

      // Webhook 模式：用 schema 各属性生成 outputs，供下游二级菜单引用
      if (
        values.mode === AgentDialogueMode.Webhook &&
        !isEmpty(values.schema)
      ) {
        outputs = transformRequestSchemaToOutput(values.schema);
      }

      const nextValues = {
        ...values,
        inputs: transferInputsArrayToObject(values.inputs),
        outputs,
      };

      updateNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, updateNodeForm, values]);
}
