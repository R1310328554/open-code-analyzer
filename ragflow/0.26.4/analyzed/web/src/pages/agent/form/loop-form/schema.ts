// schema.ts — Loop 节点表单 Zod 校验：循环变量、终止条件与最大次数。

import { z } from 'zod';

/** Loop 表单 schema：loop_variables、logical_operator、终止条件与 maximum_loop_count。 */
export const FormSchema = z.object({
  loop_variables: z.array(
    z.object({
      variable: z.string().optional(),
      type: z.string().optional(),
      value: z.string().or(z.number()).or(z.boolean()).optional(),
      input_mode: z.string(),
    }),
  ),
  logical_operator: z.string(),
  loop_termination_condition: z.array(
    z.object({
      variable: z.string().optional(),
      operator: z.string().optional(),
      value: z.string().or(z.number()).or(z.boolean()).optional(),
      input_mode: z.string().optional(),
    }),
  ),
  maximum_loop_count: z.number(),
});

/** Loop FormSchema 推导类型。 */
export type LoopFormSchemaType = z.infer<typeof FormSchema>;
