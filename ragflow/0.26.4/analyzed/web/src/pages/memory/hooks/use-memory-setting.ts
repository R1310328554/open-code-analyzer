// use-memory-setting.ts — 记忆库基础配置查询：路由 id 或 query id 拉取 IMemory。

import { useHandleSearchChange } from '@/hooks/logic-hooks';
import { IMemory } from '@/pages/memories/interface';
import memoryService from '@/services/memory-service';
import { useQuery } from '@tanstack/react-query';
import { useParams, useSearchParams } from 'react-router';
import { MemoryApiAction } from '../constant';

/** 通过 getMemoryConfig 拉取当前记忆库基础配置，支持搜索与分页参数入 queryKey。 */
export const useFetchMemoryBaseConfiguration = () => {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  // 优先 URL query id，否则使用路由 params
  const memoryBaseId = searchParams.get('id') || id;
  const { handleInputChange, searchString, pagination, setPagination } =
    useHandleSearchChange();

  const queryKey: (MemoryApiAction | number)[] = [
    MemoryApiAction.FetchMemoryDetail,
  ];

  const { data, isFetching: loading } = useQuery<IMemory>({
    queryKey: [...queryKey, searchString, pagination],
    initialData: {} as IMemory,
    // 离开页面立即丢弃缓存，避免切换记忆库读到旧数据
    gcTime: 0,
    queryFn: async () => {
      if (memoryBaseId) {
        const { data } = await memoryService.getMemoryConfig(
          memoryBaseId as string,
        );
        return data?.data ?? {};
      } else {
        return {};
      }
    },
  });

  return {
    data,
    loading,
    handleInputChange,
    searchString,
    pagination,
    setPagination,
  };
};
