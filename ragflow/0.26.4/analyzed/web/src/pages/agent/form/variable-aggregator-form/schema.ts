// schema.ts — VariableAggregator 节点 Zod 校验：groups 含 group_name、variables、type。

import { z } from 'zod';

/** 变量聚合器核心字段：分组名、组内变量值列表、输出类型。 */
export const VariableAggregatorSchema = {
  groups: z.array(
    z.object({
      group_name: z.string(),
      variables: z.array(z.object({ value: z.string().optional() })),
      type: z.string().optional(),
    }),
  ),
};

/** 完整表单 Zod schema，用于 react-hook-form resolver。 */
export const FormSchema = z.object(VariableAggregatorSchema);

/** 表单数据 TypeScript 类型推导。 */
export type VariableAggregatorFormSchemaType = z.infer<typeof FormSchema>;
