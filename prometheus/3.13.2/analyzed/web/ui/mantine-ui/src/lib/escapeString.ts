// escapeString 在 PromQL 序列化时对反斜杠与双引号加转义，保证引号内字面量合法。
// Used for escaping escape sequences and double quotes in double-quoted strings.
export const escapeString = (str: string) => {
  return str.replace(/([\\"])/g, "\\$1");
};
