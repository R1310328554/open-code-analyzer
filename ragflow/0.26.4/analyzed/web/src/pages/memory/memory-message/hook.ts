/**
 * memory-message/hook.ts — 记忆消息列表：分页搜索、Agent 筛选与删除/启停/内容预览。
 */

import { FilterCollection } from '@/components/list-filter-bar/interface';
import { useHandleFilterSubmit } from '@/components/list-filter-bar/use-handle-filter-submit';
import message from '@/components/ui/message';
import { useHandleSearchChange } from '@/hooks/logic-hooks';
import memoryService, { getMemoryDetailById } from '@/services/memory-service';
import { groupListByType } from '@/utils/list-filter-util';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { t } from 'i18next';
import { useCallback, useMemo, useState } from 'react';
import { useParams, useSearchParams } from 'react-router';
import { MemoryApiAction } from '../constant';
import {
  IMessageContentProps,
  IMessageTableProps,
} from '../memory-message/interface';
import { IMessageInfo } from './interface';

/** 按 memoryBaseId 拉取消息表格数据，支持 keywords 与 agentId 过滤。 */
export const useFetchMemoryMessageList = () => {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const memoryBaseId = searchParams.get('id') || id;
  const { handleInputChange, searchString, pagination, setPagination } =
    useHandleSearchChange();
  const { filterValue, handleFilterSubmit } = useHandleFilterSubmit();
  const queryKey: (MemoryApiAction | number)[] = [
    MemoryApiAction.FetchMemoryMessage,
  ];
  const agentIds = Array.isArray(filterValue.agentId)
    ? filterValue.agentId
    : [];
  const { data, isFetching: loading } = useQuery<IMessageTableProps>({
    queryKey: [...queryKey, searchString, pagination, filterValue],
    initialData: {} as IMessageTableProps,
    gcTime: 0,
    queryFn: async () => {
      if (memoryBaseId) {
        const { data } = await getMemoryDetailById(memoryBaseId as string, {
          keywords: searchString,
          page: pagination.current,
          page_size: pagination.pageSize,
          agentId: agentIds.length > 0 ? agentIds.join(',') : undefined,
        });
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
    filterValue,
    handleFilterSubmit,
  };
};

/** 消息行操作：删除确认、启用/禁用状态、弹窗查看 message content。 */
export const useMessageAction = () => {
  const queryClient = useQueryClient();
  const { id: memoryId } = useParams();
  const [selectedMessage, setSelectedMessage] = useState<IMessageInfo>(
    {} as IMessageInfo,
  );
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const handleClickDeleteMessage = useCallback((message: IMessageInfo) => {
    setSelectedMessage(message);
    setShowDeleteDialog(true);
  }, []);

  /** 确认删除选中 message_id 并刷新 FetchMemoryMessage 查询。 */
  const handleDeleteMessage = useCallback(() => {
    // delete message
    memoryService
      .deleteMemoryMessage({
        memory_id: memoryId,
        message_id: selectedMessage.message_id,
      })
      .then(() => {
        message.success(t('message.deleted'));
        queryClient.invalidateQueries({
          queryKey: [MemoryApiAction.FetchMemoryMessage],
        });
      });
    setShowDeleteDialog(false);
  }, [selectedMessage.message_id, queryClient]);

  /** 更新消息 status（启用/禁用），成功后 invalidate 列表。 */
  const handleUpdateMessageState = useCallback(
    (messageInfo: IMessageInfo, enable: boolean) => {
      // delete message
      const selectedMessageInfo = messageInfo || selectedMessage;
      memoryService
        .updateMessageState({
          memory_id: memoryId,
          message_id: selectedMessageInfo.message_id,
          status: enable || false,
        })
        .then((data: any) => {
          if (data.data.code === 0) {
            message.success(t('message.updated'));
            queryClient.invalidateQueries({
              queryKey: [MemoryApiAction.FetchMemoryMessage],
            });
          }
        });
      setShowDeleteDialog(false);
    },
    [selectedMessage, queryClient, memoryId],
  );

  const handleClickUpdateMessageState = useCallback(
    (message: IMessageInfo, enable: boolean) => {
      setSelectedMessage(message);
      handleUpdateMessageState(message, enable);
    },
    [handleUpdateMessageState],
  );

  const [showMessageContentDialog, setShowMessageContentDialog] =
    useState(false);
  const [selectedMessageContent, setSelectedMessageContent] =
    useState<IMessageContentProps>({} as IMessageContentProps);

  const {
    data: messageContent,
    isPending: fetchMessageContentLoading,
    mutateAsync: fetchMessageContent,
  } = useMutation<IMessageContentProps, Error, IMessageInfo>({
    mutationKey: [
      MemoryApiAction.FetchMessageContent,
      selectedMessage.message_id,
    ],

    /** 打开内容弹窗并请求 getMessageContent 填充 selectedMessageContent。 */
    mutationFn: async (selectedMessage: IMessageInfo) => {
      setShowMessageContentDialog(true);
      const res = await memoryService.getMessageContent({
        memory_id: memoryId,
        message_id: selectedMessage.message_id,
      });
      if (res.data.code === 0) {
        setSelectedMessageContent(res.data.data);
      } else {
        message.error(res.data.message);
      }
      return res.data.data;
    },
  });

  const handleClickMessageContentDialog = useCallback(
    (message: IMessageInfo) => {
      setSelectedMessage(message);
      fetchMessageContent(message);
    },
    [fetchMessageContent],
  );

  return {
    selectedMessage,
    setSelectedMessage,
    showDeleteDialog,
    setShowDeleteDialog,
    handleClickDeleteMessage,
    handleDeleteMessage,
    handleUpdateMessageState,
    messageContent,
    fetchMessageContentLoading,
    fetchMessageContent,
    selectedMessageContent,
    showMessageContentDialog,
    setShowMessageContentDialog,
    handleClickMessageContentDialog,
    handleClickUpdateMessageState,
  };
};

/** 从消息列表聚合 agent_id -> agent_name 供筛选栏使用。 */
export function useSelectFilters() {
  const { data } = useFetchMemoryMessageList();
  const agentId = useMemo(() => {
    return groupListByType(
      data?.messages?.message_list ?? [],
      'agent_id',
      'agent_name',
    );
  }, [data?.messages?.message_list]);

  const filters: FilterCollection[] = [
    {
      field: 'agentId',
      list: agentId,
      label: 'Agent',
    },
  ];

  return { filters };
}
