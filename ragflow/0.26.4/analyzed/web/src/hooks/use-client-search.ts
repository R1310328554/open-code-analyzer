// use-client-search.ts — 客户端多字段模糊搜索：防抖关键词过滤本地 data 数组。

import { useDebounce } from 'ahooks';
import { useCallback, useMemo, useState } from 'react';

/** useClientSearch 配置：数据源、搜索字段（键名或取值函数）与防抖毫秒。 */
export interface SearchFilterOptions<T> {
  data: T[];
  searchFields: Array<keyof T | ((item: T) => string)>;
  debounceMs?: number;
}

/** 对 data 按 searchFields 做 debounce 后的大小写不敏感子串匹配。 */
export function useClientSearch<T>({
  data,
  searchFields,
  debounceMs = 300,
}: SearchFilterOptions<T>) {
  const [searchKeyword, setSearchKeyword] = useState('');

  const debouncedSearchKeyword = useDebounce(searchKeyword, {
    wait: debounceMs,
  });

  const handleSearchChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setSearchKeyword(e.target.value);
    },
    [],
  );

  const clearSearch = useCallback(() => {
    setSearchKeyword('');
  }, []);

  const filteredData = useMemo(() => {
    if (!debouncedSearchKeyword.trim()) {
      return data;
    }

    const keyword = debouncedSearchKeyword.toLowerCase().trim();

    return data.filter((item) => {
      return searchFields.some((field) => {
        let value: string;

        if (typeof field === 'function') {
          value = field(item);
        } else {
          value = String(item[field] ?? '');
        }

        return value?.toLowerCase().includes(keyword);
      });
    });
  }, [data, debouncedSearchKeyword, searchFields]);

  return {
    filteredData,
    searchKeyword,
    handleSearchChange,
    clearSearch,
    isSearching: debouncedSearchKeyword !== searchKeyword,
  };
}
