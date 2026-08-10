// utils.ts — Parser 表单字段名工具：prefix 与字段名拼接为嵌套路径。

/** 生成 react-hook-form 嵌套字段名，如 prefix.lang。 */
export function buildFieldNameWithPrefix(name: string, prefix: string) {
  return `${prefix}.${name}`;
}
