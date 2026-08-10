// use-form-schema.ts — Categorize 节点 Zod 校验：LLM 参数、历史窗口与分类 items。

import { LlmSettingSchema } from '@/components/llm-setting-items/next';
import { useTranslation } from 'react-i18next';
import { z } from 'zod';

/** 构建 Categorize 表单 schema：query、parameter、LlmSetting 与 items 数组。 */
export function useCreateCategorizeFormSchema() {
  const { t } = useTranslation();

  const FormSchema = z.object({
    query: z.string().optional(),
    parameter: z.string().optional(),
    ...LlmSettingSchema,
    message_history_window_size: z.coerce.number(),
    items: z.array(
      z
        .object({
          name: z.string().min(1, t('flow.nameMessage')).trim(),
          description: z.string().optional(),
          uuid: z.string(),
          examples: z
            .array(
              z.object({
                value: z.string(),
              }),
            )
            .optional(),
        })
        .optional(),
    ),
  });

  return FormSchema;
}

/** useCreateCategorizeFormSchema 返回的 ZodObject 类型。 */
export type CreateCategorizeFormSchema = ReturnType<
  typeof useCreateCategorizeFormSchema
>;
