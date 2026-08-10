import { Path, PathValue } from "react-hook-form";

/** 键值对表单行：key 为属性名，value 为单个字符串值。 */
export type KeyValueType = { key: string; value: string };

/**
 * 将键值对数组转为 Keycloak 多值属性对象（Record<key, string[]>）。
 * 空 key 行会被忽略；同一 key 出现多次时合并为数组。
 */
export function keyValueToArray(attributeArray: KeyValueType[] = []) {
  const validAttributes = attributeArray.filter(({ key }) => key !== "");
  const result: Record<string, string[]> = {};

  for (const { key, value } of validAttributes) {
    if (key in result) {
      result[key].push(value);
    } else {
      result[key] = [value];
    }
  }

  return result;
}

/**
 * 将多值属性对象展开为表单用的键值对数组（每个数组元素一行）。
 * 返回类型与 react-hook-form 的 PathValue 对齐，便于直接写入表单状态。
 */
export function arrayToKeyValue<T>(attributes: Record<string, string[]> = {}) {
  const result = Object.entries(attributes).flatMap(([key, value]) =>
    value.map<KeyValueType>((value) => ({ key, value })),
  );

  return result as PathValue<T, Path<T>>;
}
