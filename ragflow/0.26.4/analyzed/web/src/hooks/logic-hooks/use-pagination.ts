// use-pagination.ts — 客户端内存分页：对已有数组 slice 分页，不请求后端。

import { useCallback, useMemo, useState } from 'react';

/** 对 list 做本地 slice 分页，返回 page/pageSize/pagedList 与 onPaginationChange。 */
export function useClientPagination(list: Array<any>) {
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const onPaginationChange = useCallback((page: number, pageSize: number) => {
    setPage(page);
    setPageSize(pageSize);
  }, []);

  const pagedList = useMemo(() => {
    return list?.slice((page - 1) * pageSize, page * pageSize);
  }, [list, page, pageSize]);

  return {
    page,
    pageSize,
    setPage,
    setPageSize,
    onPaginationChange,
    pagedList,
  };
}
