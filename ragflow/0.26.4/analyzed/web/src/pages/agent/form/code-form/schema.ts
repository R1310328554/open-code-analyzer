// schema.ts — Code 节点 Zod 校验：语言、脚本、arguments 与单一 output 契约。

import { ProgrammingLanguage } from '@/constants/agent';
import { z } from 'zod';
import { isValidCodeOutputName } from './utils';

/** Code 表单 schema：lang、script、arguments 数组及 output name/type 校验。 */
export const FormSchema = z.object({
  lang: z.enum([ProgrammingLanguage.Python, ProgrammingLanguage.Javascript]),
  script: z.string(),
  arguments: z.array(z.object({ name: z.string(), type: z.string() })),
  output: z.object({
    name: z
      .string()
      .trim()
      .min(1, 'Name is required')
      .refine(
        isValidCodeOutputName,
        'Name cannot use reserved outputs or path syntax',
      ),
    type: z.string().trim().min(1, 'Type is required'),
  }),
});

/** FormSchema 推导出的 TypeScript 类型。 */
export type FormSchemaType = z.infer<typeof FormSchema>;
