/**
 * memories/hooks.ts — 记忆库列表页数据层：CRUD、筛选、重命名弹窗与 React Query 缓存。
 */

// src/pages/next-memoryes/hooks.ts

import { FilterCollection } from '@/components/list-filter-bar/interface';
import { useHandleFilterSubmit } from '@/components/list-filter-bar/use-handle-filter-submit';
import message from '@/components/ui/message';
import { useSetModalState } from '@/hooks/common-hooks';
import { useHandleSearchChange } from '@/hooks/logic-hooks';
import { useFetchDefaultModelDictionary } from '@/hooks/use-llm-request';
import memoryService, { updateMemoryById } from '@/services/memory-service';
import {
  buildOwnersFilter,
  groupListByArray,
  groupListByType,
} from '@/utils/list-filter-util';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useDebounce } from 'ahooks';
import { omit } from 'lodash';
import { useCallback, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useSearchParams } from 'react-router';
import {
  CreateMemoryResponse,
  DeleteMemoryProps,
  DeleteMemoryResponse,
  ICreateMemoryProps,
  IMemory,
  IMemoryAppDetailProps,
  MemoryDetailResponse,
  MemoryListResponse,
} from './interface';

/** 调用 createMemory 接口并在成功时弹出 created 提示。 */
export const useCreateMemory = () => {
  const { t } = useTranslation();

  const createMemory = useCallback(
    async (props: ICreateMemoryProps): Promise<CreateMemoryResponse> => {
      const { data: response } = await memoryService.createMemory(props);
      if (response.code !== 0) {
        throw new Error(response.message || 'Failed to create memory');
      }
      if (response.code === 0) {
        message.success(t('message.created'));
      }
      return response.data;
    },
    [t],
  );

  return { createMemory };
};

/** 分页 + 关键词 + memoryType/storageType/owner 筛选拉取记忆库列表。 */
export const useFetchMemoryList = () => {
  const { handleInputChange, searchString, pagination, setPagination } =
    useHandleSearchChange();
  const { filterValue, handleFilterSubmit } = useHandleFilterSubmit();
  // 搜索框输入防抖 500ms，减少列表接口频率
  const debouncedSearchString = useDebounce(searchString, { wait: 500 });

  const memoryType = Array.isArray(filterValue.memoryType)
    ? filterValue.memoryType
    : [];
  const storageType = Array.isArray(filterValue.storageType)
    ? filterValue.storageType
    : [];
  const owner = filterValue.owner;
  const requestParams: Record<string, any> = {
    keywords: debouncedSearchString,
    page_size: pagination.pageSize,
    page: pagination.current,
    memory_type: memoryType.length > 0 ? memoryType.join(',') : undefined,
    storage_type: storageType.length === 1 ? storageType[0] : undefined,
  };

  if (Array.isArray(owner) && owner.length > 0) {
    requestParams.owner_ids = owner.join(',');
  }
  const { data, isLoading, isError, refetch } = useQuery<
    MemoryListResponse,
    Error
  >({
    queryKey: [
      'memoryList',
      {
        debouncedSearchString,
        ...pagination,
      },
      filterValue,
    ],
    queryFn: async () => {
      const { data: response } = await memoryService.getMemoryList(
        {
          params: requestParams,
          data: { memory_type: memoryType },
        },
        true,
      );
      if (response.code !== 0) {
        throw new Error(response.message || 'Failed to fetch memory list');
      }
      console.log(response);
      return response;
    },
  });

  // const setMemoryListParams = (newParams: MemoryListParams) => {
  //   setMemoryParams((prevParams) => ({
  //     ...prevParams,
  //     ...newParams,
  //   }));
  // };

  return {
    data,
    isLoading,
    isError,
    pagination,
    searchString,
    handleInputChange,
    setPagination,
    refetch,
    filterValue,
    handleFilterSubmit,
  };
};

/** 按路由 id 或 shared_id 拉取记忆库详情；分享链接需 tenantId。 */
export const useFetchMemoryDetail = (tenantId?: string) => {
  const { id } = useParams();

  const [memoryParams] = useSearchParams();
  const shared_id = memoryParams.get('shared_id');
  const memoryId = id || shared_id;
  let param: { id: string | null; tenant_id?: string } = {
    id: memoryId,
  };
  if (shared_id) {
    param = {
      id: memoryId,
      tenant_id: tenantId,
    };
  }
  // 分享场景走 getMemoryDetailShare，否则走常规详情接口
  const fetchMemoryDetailFunc = shared_id
    ? memoryService.getMemoryDetailShare
    : memoryService.getMemoryDetail;

  const { data, isLoading, isError } = useQuery<MemoryDetailResponse, Error>({
    queryKey: ['memoryDetail', memoryId],
    enabled: !shared_id || !!tenantId,
    queryFn: async () => {
      const { data: response } = await fetchMemoryDetailFunc(param);
      if (response.code !== 0) {
        throw new Error(response.message || 'Failed to fetch memory detail');
      }
      return response;
    },
  });

  return { data: data?.data, isLoading, isError };
};

/** 删除记忆库并在成功后 invalidate memoryList 查询。 */
export const useDeleteMemory = () => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const {
    data,
    isError,
    mutateAsync: deleteMemoryMutation,
  } = useMutation<DeleteMemoryResponse, Error, DeleteMemoryProps>({
    mutationKey: ['deleteMemory'],
    mutationFn: async (props) => {
      const { data: response } = await memoryService.deleteMemory(
        props.memory_id,
      );
      if (response.code !== 0) {
        throw new Error(response.message || 'Failed to delete memory');
      }

      queryClient.invalidateQueries({ queryKey: ['memoryList'] });
      return response;
    },
    onSuccess: () => {
      message.success(t('message.deleted'));
    },
    onError: (error) => {
      message.error(t('message.error', { error: error.message }));
    },
  });

  const deleteMemory = useCallback(
    (props: DeleteMemoryProps) => {
      return deleteMemoryMutation(props);
    },
    [deleteMemoryMutation],
  );

  return { data, isError, deleteMemory };
};

/** 按 id 更新记忆库配置，成功后刷新 memoryDetail 缓存。 */
export const useUpdateMemory = () => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const {
    data,
    isError,
    mutateAsync: updateMemoryMutation,
  } = useMutation<any, Error, IMemoryAppDetailProps>({
    mutationKey: ['updateMemory'],
    mutationFn: async (formData) => {
      const param = omit(formData, ['id']);
      const { data: response } = await updateMemoryById(formData.id, param);
      if (response.code !== 0) {
        throw new Error(response.message || 'Failed to update memory');
      }

      return response.data;
    },
    onSuccess: (data, variables) => {
      message.success(t('message.updated'));
      queryClient.invalidateQueries({
        queryKey: ['memoryDetail', variables.id],
      });
    },
  });

  const updateMemory = useCallback(
    (formData: IMemoryAppDetailProps) => {
      return updateMemoryMutation(formData);
    },
    [updateMemoryMutation],
  );

  return { data, isError, updateMemory };
};

/** 新建/重命名弹窗：无 id 时 create，有 id 时 update 名称。 */
export const useRenameMemory = () => {
  const [memory, setMemory] = useState<IMemory>({} as IMemory);
  const {
    visible: openCreateModal,
    hideModal: hideChatRenameModal,
    showModal: showChatRenameModal,
  } = useSetModalState();
  const { updateMemory } = useUpdateMemory();
  const { createMemory } = useCreateMemory();
  const [loading, setLoading] = useState(false);
  const defaultModelDictionary = useFetchDefaultModelDictionary();

  const handleShowChatRenameModal = useCallback(
    (record?: IMemory) => {
      if (record) {
        // 打开编辑弹窗时补齐默认 embedding / LLM 模型 ID
        const embd_id = record.embd_id || defaultModelDictionary?.embd_id;
        const llm_id = record.llm_id || defaultModelDictionary?.llm_id;
        setMemory({
          ...record,
          embd_id,
          llm_id,
        });
      }
      showChatRenameModal();
    },
    [showChatRenameModal, defaultModelDictionary],
  );

  const handleHideModal = useCallback(() => {
    hideChatRenameModal();
    setMemory({} as IMemory);
  }, [hideChatRenameModal]);

  const onMemoryRenameOk = useCallback(
    async (data: ICreateMemoryProps, callBack?: () => void) => {
      // let res;
      setLoading(true);
      if (memory?.id) {
        try {
          await updateMemory({
            // ...memoryDataTemp,
            name: data.name,
            id: memory?.id,
          } as unknown as IMemoryAppDetailProps);
        } catch (e) {
          console.error('error', e);
        }
      } else {
        await createMemory(data);
      }
      // if (res && !memory?.id) {
      //   navigateToMemory(res?.id)();
      // }
      callBack?.();
      setLoading(false);
      handleHideModal();
    },
    [memory, createMemory, handleHideModal, updateMemory],
  );
  return {
    memoryRenameLoading: loading,
    initialMemory: memory,
    onMemoryRenameOk,
    openCreateModal,
    hideMemoryModal: handleHideModal,
    showMemoryRenameModal: handleShowChatRenameModal,
  };
};

/** 从当前列表数据聚合 memoryType、storageType 与 owner 筛选项。 */
export function useSelectFilters() {
  const { t } = useTranslation();
  const { data: res } = useFetchMemoryList();
  const data = res?.data;

  const memoryType = useMemo(() => {
    return groupListByArray(data?.memory_list ?? [], 'memory_type');
  }, [data?.memory_list]);
  const storageType = useMemo(() => {
    return groupListByType(
      data?.memory_list ?? [],
      'storage_type',
      'storage_type',
    );
  }, [data?.memory_list]);

  const filters: FilterCollection[] = [
    buildOwnersFilter(data?.memory_list ?? [], 'owner_name', t('common.owner')),
    {
      field: 'memoryType',
      list: memoryType,
      label: t('memories.memoryType'),
    },
    {
      field: 'storageType',
      list: storageType,
      label: t('memory.config.storageType'),
    },
  ];

  return { filters };
}
