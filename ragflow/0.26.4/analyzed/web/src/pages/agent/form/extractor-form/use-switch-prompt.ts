// use-switch-prompt.ts — Extractor 字段名切换：确认后按 i18n 模板更新 sys/user prompt。

import { LlmSettingSchema } from '@/components/llm-setting-items/next';
import { useSetModalState } from '@/hooks/common-hooks';
import { useCallback, useRef } from 'react';
import { UseFormReturn } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { z } from 'zod';

/** Extractor 表单 schema：field_name、sys_prompt、prompts 及 LLM 设置。 */
export const FormSchema = z.object({
  field_name: z.string(),
  sys_prompt: z.string(),
  prompts: z.string().optional(),
  ...LlmSettingSchema,
});

/** Extractor FormSchema 推导类型。 */
export type ExtractorFormSchemaType = z.infer<typeof FormSchema>;

/** 字段名变更时弹窗确认；确认则切换 prompt 模板，取消则恢复上一字段名。 */
export function useSwitchPrompt(form: UseFormReturn<ExtractorFormSchemaType>) {
  const { visible, showModal, hideModal } = useSetModalState();
  const { t } = useTranslation();
  const previousFieldNames = useRef<string[]>([form.getValues('field_name')]);

  /** 从 flow.prompts i18n 键写入指定表单字段。 */
  const setPromptValue = useCallback(
    (field: keyof ExtractorFormSchemaType, key: string, value: string) => {
      form.setValue(field, t(`flow.prompts.${key}.${value}`), {
        shouldDirty: true,
        shouldValidate: true,
      });
    },
    [form, t],
  );

  const handleFieldNameChange = useCallback(
    (value: string) => {
      if (value) {
        const names = previousFieldNames.current;
        if (names.length > 1) {
          names.shift();
        }
        names.push(value);
        showModal();
      }
    },
    [showModal],
  );

  /** 确认切换：按当前 field_name 更新 sys_prompt 与 prompts。 */
  const confirmSwitch = useCallback(() => {
    const value = form.getValues('field_name');
    setPromptValue('sys_prompt', 'system', value);
    setPromptValue('prompts', 'user', value);
  }, [form, setPromptValue]);

  /** 取消切换：恢复 previousFieldNames 中的上一个字段名。 */
  const cancelSwitch = useCallback(() => {
    const previousValue = previousFieldNames.current.at(-2);
    if (previousValue) {
      form.setValue('field_name', previousValue, {
        shouldDirty: true,
        shouldValidate: true,
      });
    }
  }, [form]);

  return {
    handleFieldNameChange,
    confirmSwitch,
    hideModal,
    visible,
    cancelSwitch,
  };
}
