// utils.ts — Dataflow 结果页分块 tag_feas 表单与对象互转（与知识库 utils 同构）。

/** 标签特征表单项：频次 + 标签名。 */
export type FormListItem = {
  frequency: number;
  tag: string;
};

/** 表单数组 → API 所需的 tag_feas 对象。 */
export function transformTagFeaturesArrayToObject(
  list: Array<FormListItem> = [],
) {
  return list.reduce<Record<string, number>>((pre, cur) => {
    pre[cur.tag] = cur.frequency;

    return pre;
  }, {});
}

/** API tag_feas 对象 → 可编辑表单列表。 */
export function transformTagFeaturesObjectToArray(
  object: Record<string, number> = {},
) {
  return Object.keys(object).reduce<Array<FormListItem>>((pre, key) => {
    pre.push({ frequency: object[key], tag: key });

    return pre;
  }, []);
}
