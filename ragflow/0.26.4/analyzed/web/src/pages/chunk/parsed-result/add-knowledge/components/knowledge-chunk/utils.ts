// utils.ts — 分块标签特征（tag_feas）在表单数组与对象结构间互转。

/** 表单中一条标签特征：标签名与出现频次。 */
export type FormListItem = {
  frequency: number;
  tag: string;
};

/** 将 [{ tag, frequency }] 转为 Record<tag, frequency> 供 API 提交。 */
export function transformTagFeaturesArrayToObject(
  list: Array<FormListItem> = [],
) {
  return list.reduce<Record<string, number>>((pre, cur) => {
    pre[cur.tag] = cur.frequency;

    return pre;
  }, {});
}

/** 将 API 返回的 tag_feas 对象还原为表单可编辑的数组。 */
export function transformTagFeaturesObjectToArray(
  object: Record<string, number> = {},
) {
  return Object.keys(object).reduce<Array<FormListItem>>((pre, key) => {
    pre.push({ frequency: object[key], tag: key });

    return pre;
  }, []);
}
