/**
 * 按当前管理员 locale 对列表项进行 localeCompare 排序的 Hook。
 */
import { useWhoAmI } from "../context/whoami/WhoAmI";

/** 从列表项提取用于比较的字符串；undefined 时参与排序的项视为相等。 */
export type ValueMapperFn<T> = (item: T) => string | undefined;

/**
 * 返回 localeSort 函数：拷贝数组后按 mapper 提取的字符串、使用 whoAmI.locale 排序。
 */
export default function useLocaleSort() {
  const { whoAmI } = useWhoAmI();

  return function localeSort<T>(items: T[], mapperFn: ValueMapperFn<T>): T[] {
    return [...items].sort((a, b) => {
      const valA = mapperFn(a);
      const valB = mapperFn(b);

      if (valA === undefined || valB === undefined) {
        return 0;
      }

      return valA.localeCompare(valB, whoAmI.locale);
    });
  };
}

// TODO: This might be built into TypeScript into future.
// See: https://github.com/microsoft/TypeScript/issues/48992
/** 映射类型 T 中值类型为 V 的键名联合（条件类型工具）。 */
type KeysMatching<T, V> = {
  [K in keyof T]: T[K] extends V ? K : never;
}[keyof T];

/**
 * 生成按对象指定字符串键取值的 mapper，供 localeSort 与表格排序复用。
 */
export const mapByKey =
  <
    T extends { [_ in K]?: string },
    K extends KeysMatching<T, string | undefined>,
  >(
    key: K,
  ) =>
  (item: T) =>
    item[key];
