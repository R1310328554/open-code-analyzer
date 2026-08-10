// use-handle-filter-submit.ts — 列表筛选提交 Hook：维护 filterValue 并与分页路由联动。

import { useGetPaginationWithRouter } from '@/hooks/logic-hooks';
import { useCallback, useState } from 'react';
import {
  FilterChange,
  FilterCollection,
  FilterType,
  FilterValue,
} from './interface';

/** 递归收集 FilterType 树中所有叶子筛选项 id。 */
const getFilterIds = (filter: FilterType): string[] => {
  let ids: string[] = [];
  if (!filter.list) {
    ids = [filter.id];
  }

  if (filter.list && Array.isArray(filter.list)) {
    for (const item of filter.list) {
      ids = ids.concat(getFilterIds(item));
    }
  }

  return ids;
};

/** 用有效 id 列表裁剪现有 filterValue，移除已失效选中项。 */
const mergeFilterValue = (
  filterValue: FilterValue,
  ids: string[],
): FilterValue => {
  const value = {} as FilterValue;
  for (const key in filterValue) {
    if (Array.isArray(filterValue[key])) {
      const keyIds = filterValue[key] as string[];
      value[key] = ids.filter((id) => keyIds.includes(id));
    } else if (typeof filterValue[key] === 'object') {
      value[key] = mergeFilterValue(filterValue[key], ids);
    }
  }
  return value;
};
/** 管理筛选状态：提交时重置页码，并在筛选项变化时校验选中值。 */
export function useHandleFilterSubmit() {
  const [filterValue, setFilterValue] = useState<FilterValue>({});
  const { setPagination } = useGetPaginationWithRouter();
  const handleFilterSubmit: FilterChange = useCallback(
    (value) => {
      setFilterValue(value);
      setPagination({ page: 1 });
    },
    [setPagination],
  );

  const checkValue = useCallback((filters: FilterCollection[]) => {
    if (!filters?.length) {
      return;
    }

    const validFields = filters.reduce((pre, cur) => {
      return [...pre, ...getFilterIds(cur as FilterType)];
    }, [] as string[]);

    if (!validFields.length) {
      return;
    }

    setFilterValue((preValue) => {
      if (!preValue) return preValue;

      const newValue: FilterValue = mergeFilterValue(preValue, validFields);
      return newValue;
    });
  }, []);

  return { filterValue, setFilterValue, handleFilterSubmit, checkValue };
}
