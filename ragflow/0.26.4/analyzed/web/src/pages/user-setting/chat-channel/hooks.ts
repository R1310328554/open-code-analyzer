/**
 * chat-channel/hooks.ts — 聊天渠道（WhatsApp 等）列表、增删改与助手绑定。
 */

import message from '@/components/ui/message';
import { useSetModalState } from '@/hooks/common-hooks';
import chatChannelService, {
  deleteChatChannel,
  fetchChatChannelDetail,
  updateChatChannel,
} from '@/services/chat-channel-service';
import chatService from '@/services/next-chat-service';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { t } from 'i18next';
import { useCallback, useMemo, useState } from 'react';
import { ChatChannelKey, useChatChannelInfo } from './constant';
import { IChatChannel, IChatChannelBase, IChatChannelInfo } from './interface';

/** React Query 缓存键：列表、详情与可选助手列表。 */
export const ChatChannelKeys = {
  list: () => ['chat-channel'] as const,
  detail: (id?: string) => ['chat-channel-detail', id] as const,
  dialogs: () => ['chat-channel-dialogs'] as const,
};

/** 拉取渠道实例并按 ChatChannelKey 分组展示。 */
export const useListChatChannel = () => {
  const { chatChannelInfo } = useChatChannelInfo();
  const { data: list, isFetching } = useQuery<IChatChannelBase[]>({
    queryKey: ChatChannelKeys.list(),
    queryFn: async () => {
      const { data } = await chatChannelService.chatChannelList();
      return data.data;
    },
  });

  const categorizedList = useMemo(() => {
    const grouped: Partial<Record<ChatChannelKey, IChatChannelBase[]>> = {};
    (list || []).forEach((item) => {
      const channel = item.channel;
      if (!grouped[channel]) {
        grouped[channel] = [];
      }
      grouped[channel]!.push(item);
    });

    const result: Array<IChatChannelInfo & { list: IChatChannelBase[] }> = [];
    (Object.keys(grouped) as ChatChannelKey[]).forEach((key) => {
      if (chatChannelInfo[key]) {
        result.push({
          id: key,
          name: chatChannelInfo[key].name,
          description: chatChannelInfo[key].description,
          icon: chatChannelInfo[key].icon,
          list: grouped[key] || [],
        });
      }
    });
    return result;
  }, [list, chatChannelInfo]);

  return { list, categorizedList, isFetching };
};

/** 新建/编辑渠道弹窗；WhatsApp 新建后保留弹窗以完成 OAuth 等步骤。 */
export const useAddChatChannel = () => {
  const [activeChannel, setActiveChannel] = useState<
    IChatChannelInfo | undefined
  >(undefined);
  const [editingRecord, setEditingRecord] = useState<IChatChannel | undefined>(
    undefined,
  );
  const [loading, setLoading] = useState(false);
  const { visible: modalVisible, hideModal, showModal } = useSetModalState();
  const queryClient = useQueryClient();

  const showAddingModal = useCallback(
    (channel: IChatChannelInfo) => {
      setEditingRecord(undefined);
      setActiveChannel(channel);
      showModal();
    },
    [showModal],
  );

  const showEditingModal = useCallback(
    (channel: IChatChannelInfo, record: IChatChannel) => {
      setEditingRecord(record);
      setActiveChannel(channel);
      showModal();
    },
    [showModal],
  );

  const handleOk = useCallback(
    async (values: any) => {
      setLoading(true);
      try {
        const isEdit = Boolean(values?.id);
        const { data: res } = isEdit
          ? await updateChatChannel(values.id, {
              name: values.name,
              config: values.config,
            })
          : await chatChannelService.chatChannelSet(values);
        if (res.code === 0) {
          queryClient.invalidateQueries({ queryKey: ChatChannelKeys.list() });
          if (isEdit) {
            queryClient.invalidateQueries({
              queryKey: ChatChannelKeys.detail(values.id),
            });
          } else if (values.channel === ChatChannelKey.WHATSAPP) {
            setEditingRecord(res.data as IChatChannel);
            queryClient.invalidateQueries({
              queryKey: ChatChannelKeys.detail(res.data?.id),
            });
          }
          message.success(t('message.operated'));
          if (isEdit || values.channel !== ChatChannelKey.WHATSAPP) {
            hideModal();
          }
        }
      } finally {
        setLoading(false);
      }
    },
    [hideModal, queryClient],
  );

  return {
    activeChannel,
    editingRecord,
    loading,
    modalVisible,
    hideModal,
    showAddingModal,
    showEditingModal,
    handleOk,
  };
};

/** 删除渠道实例并刷新列表。 */
export const useDeleteChatChannel = () => {
  const queryClient = useQueryClient();
  const { mutateAsync, isPending } = useMutation({
    mutationKey: ['delete-chat-channel'],
    mutationFn: async (id: string) => {
      const { data } = await deleteChatChannel(id);
      if (data.code === 0) {
        message.success(t('message.deleted'));
        queryClient.invalidateQueries({ queryKey: ChatChannelKeys.list() });
      }
      return data;
    },
  });
  return { handleDelete: mutateAsync, deleteLoading: isPending };
};

/** 按需拉取单个渠道详情（含 config）。 */
export const useFetchChatChannelDetail = () => {
  const fetchDetail = useCallback(
    async (id: string): Promise<IChatChannel | undefined> => {
      const { data } = await fetchChatChannelDetail(id);
      if (data.code === 0) {
        return data.data;
      }
      return undefined;
    },
    [],
  );
  return { fetchDetail };
};

/** 将渠道绑定或解绑到指定助手（dialog）；dialogId 为 null 表示断开。 */
// Connect (or disconnect) a chat channel to an assistant (dialog).
export const useConnectChatChannelDialog = () => {
  const queryClient = useQueryClient();

  const { mutateAsync, isPending } = useMutation({
    mutationKey: ['connect-chat-channel-dialog'],
    mutationFn: async (params: {
      channelId: string;
      dialogId: string | null;
    }) => {
      const { data } = await updateChatChannel(params.channelId, {
        chat_id: params.dialogId,
      });
      if (data.code === 0) {
        message.success(t('message.operated'));
        queryClient.invalidateQueries({ queryKey: ChatChannelKeys.list() });
      }
      return data;
    },
  });

  return { connect: mutateAsync, connecting: isPending };
};

/** 拉取可绑定的助手列表（分页 100 条）。 */
// Assistants (dialogs) available to connect a channel to.
export const useChatChannelDialogList = () => {
  const { data, isFetching } = useQuery<Array<{ id: string; name: string }>>({
    queryKey: ChatChannelKeys.dialogs(),
    initialData: [],
    queryFn: async () => {
      const { data } = await chatService.listChats(
        { params: { page_size: 100, page: 1 }, data: {} },
        true,
      );
      return data?.data?.chats ?? [];
    },
  });
  return { dialogs: data, isFetching };
};
