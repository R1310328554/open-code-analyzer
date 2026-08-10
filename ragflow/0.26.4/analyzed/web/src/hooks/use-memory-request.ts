// use-memory-request.ts — Agent 记忆列表查询 Hook。

import { IMemory } from '@/interfaces/database/memory';
import memoryService from '@/services/memory-service';
import { useQuery } from '@tanstack/react-query';

/** 记忆相关 React Query 缓存键枚举。 */
export const enum MemoryApiAction {
  FetchMemoryList = 'fetchMemoryList',
}

/** 拉取全部记忆条目（首页 100 条），供下拉或选择器使用。 */
export const useFetchAllMemoryList = () => {
  const { data, isLoading, isError, refetch } = useQuery<IMemory[], Error>({
    queryKey: [MemoryApiAction.FetchMemoryList],
    queryFn: async () => {
      const { data: response } = await memoryService.getMemoryList(
        {
          params: { page_size: 100, page: 1 },
          data: {},
        },
        true,
      );
      return response.data.memory_list ?? [];
    },
  });

  return {
    data,
    isLoading,
    isError,
    refetch,
  };
};
