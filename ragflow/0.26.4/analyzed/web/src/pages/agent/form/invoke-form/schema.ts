// schema.ts — Invoke HTTP 表单 Zod 校验：URL 占位符与 variables 数组。

import { z } from 'zod';

/** 单条 Invoke 变量：key、ref、value。 */
export const VariableFormSchema = z.object({
  key: z.string(),
  ref: z.string(),
  value: z.string(),
});

// 占位符示例：{user_id} 或 {component@variable}
const placeholderRegex = /\{([a-zA-Z_][a-zA-Z0-9_.@-]*)\}/g;

// URL 校验：支持标准 URL 与含 {占位符} 的模板 URL
// 1. Standard URLs (e.g. https://example.com/api)
// 2. URLs with variable placeholders in curly braces (e.g. https://api/{user_id}/posts)
const urlValidation = z.string().refine(
  (val) => {
    if (!val) return false;

    const hasPlaceholders = val.includes('{') && val.includes('}');
    const matches = [...val.matchAll(placeholderRegex)];

    if (hasPlaceholders) {
      if (
        !matches.length ||
        matches.some((m) => !/^[a-zA-Z_][a-zA-Z0-9_.@-]*$/.test(m[1]))
      )
        return false;

      if ((val.match(/{/g) || []).length !== (val.match(/}/g) || []).length)
        return false;

      const testURL = val.replace(placeholderRegex, 'placeholder');

      return isValidURL(testURL);
    }

    return isValidURL(val);
  },
  {
    message: 'Must be a valid URL or URL with variable placeholders',
  },
);

/** 校验 URL：补全 http 协议或允许以 / 开头的相对路径。 */
function isValidURL(str: string): boolean {
  try {
    // Try to construct a full URL; prepend http:// if protocol is missing
    new URL(str.startsWith('http') ? str : `http://${str}`);
    return true;
  } catch {
    // Allow relative paths (e.g. /api/users) if needed
    return /^\/[a-zA-Z0-9]/.test(str);
  }
}

/** Invoke 完整表单：url、method、timeout、headers、proxy、variables 等。 */
export const FormSchema = z.object({
  url: urlValidation,
  method: z.string(),
  timeout: z.number(),
  headers: z.string(),
  proxy: z.string().url(),
  clean_html: z.boolean(),
  variables: z.array(VariableFormSchema),
});

/** Invoke FormSchema 推导类型。 */
export type FormSchemaType = z.infer<typeof FormSchema>;

/** 单条 variable 表单类型。 */
export type VariableFormSchemaType = z.infer<typeof VariableFormSchema>;
