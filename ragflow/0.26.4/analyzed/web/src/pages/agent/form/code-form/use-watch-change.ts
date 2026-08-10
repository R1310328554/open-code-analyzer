// use-watch-change.ts — Code 表单监听：arguments 对象化、output 契约序列化与语言切换。

import { CodeTemplateStrMap, ProgrammingLanguage } from '@/constants/agent';
import { isEmpty } from 'lodash';
import { useCallback, useEffect } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import useGraphStore from '../../store';
import { FormSchemaType } from './schema';
import {
  buildDefaultCodeOutput,
  hasLegacyMultiOutputs,
  serializeCodeOutputContract,
} from './utils';

/** 将 arguments 数组还原为 DSL 所需的 Record<string, string>。 */
function convertToObject(list: FormSchemaType['arguments'] = []) {
  return list.reduce<Record<string, string>>((pre, cur) => {
    pre[cur.name] = cur.type;

    return pre;
  }, {});
}

/** 脏变更时写回 Code 节点：arguments 对象化，output 序列化为 outputs 并删顶层 output。 */
export function useWatchFormChange(
  id?: string,
  form?: UseFormReturn<FormSchemaType>,
) {
  const watchedValues = useWatch({ control: form?.control });
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);
  const getNode = useGraphStore((state) => state.getNode);

  useEffect(() => {
    // 用户编辑触发的表单变更同步到画布
    if (id) {
      const values = form?.getValues() || watchedValues || {};
      const currentOutputs = getNode(id)?.data?.form?.outputs;
      // 多 output 旧图且 output 字段未脏改时保留现有 outputs
      const shouldPreserveLegacyOutputs =
        hasLegacyMultiOutputs(currentOutputs) &&
        isEmpty(form?.formState.dirtyFields?.output);
      const hasCompleteOutputContract =
        !!values?.output?.name?.trim() && !!values?.output?.type?.trim();
      const nextValues: any = {
        ...values,
        arguments: convertToObject(
          values?.arguments as FormSchemaType['arguments'],
        ),
        outputs: shouldPreserveLegacyOutputs
          ? currentOutputs
          : hasCompleteOutputContract
            ? serializeCodeOutputContract({
                name: values.output?.name?.trim() ?? '',
                type: values.output?.type?.trim() ?? '',
              })
            : (currentOutputs ??
              serializeCodeOutputContract(buildDefaultCodeOutput())),
      };
      delete nextValues.output;

      updateNodeForm(id, nextValues);
    }
  }, [
    form?.formState.dirtyFields?.output,
    form?.formState.isDirty,
    form,
    getNode,
    id,
    updateNodeForm,
    watchedValues,
  ]);
}

/** 切换编程语言时替换 script 模板，必要时补默认 output 并局部 updateNodeForm。 */
export function useHandleLanguageChange(
  id?: string,
  form?: UseFormReturn<FormSchemaType>,
) {
  const updateNodeForm = useGraphStore((state) => state.updateNodeForm);

  const handleLanguageChange = useCallback(
    (lang: string) => {
      if (id) {
        const script = CodeTemplateStrMap[lang as ProgrammingLanguage];
        form?.setValue('script', script);
        if (
          !form?.getValues('output')?.name ||
          !form?.getValues('output')?.type
        ) {
          form?.setValue('output', buildDefaultCodeOutput(), {
            shouldDirty: true,
          });
        }
        updateNodeForm(id, script, ['script']);
      }
    },
    [form, id, updateNodeForm],
  );

  return handleLanguageChange;
}
