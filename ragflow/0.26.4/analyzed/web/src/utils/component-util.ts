/**
 * component-util.ts — UI 组件辅助：将列表数据映射为 Select 的 label/value 选项。
 */

/** 将数组转为 { label, value } 选项；可指定键名映射，否则 label/value 均为元素本身。 */
export function buildSelectOptions(
  list: Array<any>,
  keyName?: string,
  valueName?: string,
) {
  if (!Array.isArray(list) || !list.length) {
    return [];
  }
  if (keyName && valueName) {
    return list.map((x) => ({ label: x[valueName], value: x[keyName] }));
  }
  return list.map((x) => ({ label: x, value: x }));
}
