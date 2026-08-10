/**
 * list-filter-util.ts — 列表筛选聚合：按字段分组计数、数组字段展开与 Owner 过滤器构建。
 */

/** 筛选项：id、展示 label 与出现次数 count。 */
export type FilterType = {
  id: string;
  label: string;
  count: number;
};

/** 按 idField 分组统计，label 取自 labelField。 */
export function groupListByType<T extends Record<string, any>>(
  list: T[],
  idField: string,
  labelField: string,
) {
  const fileTypeList: FilterType[] = [];
  if (Array.isArray(list)) {
    list.forEach((x) => {
      const item = fileTypeList.find((y) => y.id === x[idField]);
      if (!item) {
        fileTypeList.push({ id: x[idField], label: x[labelField], count: 1 });
      } else {
        item.count += 1;
      }
    });
  }

  return fileTypeList;
}

/** 对列表中数组型 idField 逐项展开并计数（如多标签）。 */
export function groupListByArray<T extends Record<string, any>>(
  list: T[],
  idField: string,
) {
  const fileTypeList: FilterType[] = [];
  list.forEach((x) => {
    if (Array.isArray(x[idField])) {
      x[idField].forEach((j) => {
        const item = fileTypeList.find((i) => i.id === j);
        if (!item) {
          fileTypeList.push({ id: j, label: j, count: 1 });
        } else {
          item.count += 1;
        }
      });
    }
  });

  return fileTypeList;
}

/** 按 tenant_id 聚合 Owner 筛选项，返回 field/list/label 结构。 */
export function buildOwnersFilter<T extends Record<string, any>>(
  list: T[],
  nickName?: string,
  label?: string,
) {
  const owners = groupListByType(list, 'tenant_id', nickName || 'nickname');

  return { field: 'owner', list: owners, label: label ?? 'Owner' };
}
