/**
 * 将键值对象序列化为 URL 查询字符串（application/x-www-form-urlencoded 格式）。
 * 自动跳过 undefined、null、空字符串与空数组。
 */
export function stringifyQueryParams(params: Record<string, unknown>) {
  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    // Ignore undefined and null values.
    // 忽略未定义与 null 值
    if (value === undefined || value === null) {
      continue;
    }

    // Ignore empty strings.
    // 忽略空字符串
    if (typeof value === "string" && value.length === 0) {
      continue;
    }

    // Ignore empty arrays.
    // 忽略空数组
    if (Array.isArray(value) && value.length === 0) {
      continue;
    }

    // Append each entry of an array as a separate parameter, or the value itself otherwise.
    // 数组展开为同名多值参数；标量直接 append
    if (Array.isArray(value)) {
      value.forEach((item) => searchParams.append(key, item.toString()));
    } else {
      searchParams.append(key, value.toString());
    }
  }

  return searchParams.toString();
}
