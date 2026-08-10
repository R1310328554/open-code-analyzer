// use-submit-form.ts — ExeSQL 表单 schema 与提交：数据库连接测试。

import { useTestDbConnect } from '@/hooks/use-agent-request';
import { useCallback } from 'react';
import { z } from 'zod';

/** ExeSQL 数据库连接字段 Zod 校验规则。 */
export const ExeSQLFormSchema = {
  db_type: z.string().min(1),
  database: z.string().min(1),
  username: z.string().min(1),
  host: z.string().min(1),
  port: z.number(),
  password: z.string().optional().or(z.literal('')),
  max_records: z.number(),
};

/** 完整表单 schema：sql + 连接信息；非 trino 时 password 必填。 */
export const FormSchema = z
  .object({
    sql: z.string().optional(),
    ...ExeSQLFormSchema,
  })
  .superRefine((v, ctx) => {
    if (
      v.db_type !== 'trino' &&
      !(v.password && v.password.trim().length > 0)
    ) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['password'],
        message: 'String must contain at least 1 character(s)',
      });
    }
  });

/** 提交时调用 testDbConnect 验证数据库连通性。 */
export function useSubmitForm() {
  const { testDbConnect, loading } = useTestDbConnect();

  const onSubmit = useCallback(
    async (data: z.infer<typeof FormSchema>) => {
      testDbConnect(data);
    },
    [testDbConnect],
  );

  return { loading, onSubmit };
}
