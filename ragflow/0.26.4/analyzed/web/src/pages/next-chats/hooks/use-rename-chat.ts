// use-rename-chat.ts — 聊天助手重命名弹窗与 patchChat 请求。

import { useSetModalState } from '@/hooks/common-hooks';
import { usePatchChat } from '@/hooks/use-chat-request';
import { IDialog } from '@/interfaces/database/chat';
import { useCallback, useState } from 'react';

/** 管理助手重命名弹窗：记录当前 IDialog 并调用 patchChat 更新名称。 */
export const useRenameChat = () => {
  const [chat, setChat] = useState<IDialog>({} as IDialog);
  const {
    visible: chatRenameVisible,
    hideModal: hideChatRenameModal,
    showModal: showChatRenameModal,
  } = useSetModalState();
  const { patchChat, loading: patchLoading } = usePatchChat();

  /** 确认重命名：patch 成功后关闭弹窗。 */
  const onChatRenameOk = useCallback(
    async (name: string) => {
      const ret = await patchChat({
        chatId: chat.id,
        params: { name },
      });

      if (ret === 0) {
        hideChatRenameModal();
      }
    },
    [chat.id, patchChat, hideChatRenameModal],
  );

  /** 打开弹窗前缓存待重命名的助手记录。 */
  const handleShowChatRenameModal = useCallback(
    (record: IDialog) => {
      setChat(record);
      showChatRenameModal();
    },
    [showChatRenameModal],
  );

  /** 关闭弹窗并清空临时 chat 状态。 */
  const handleHideModal = useCallback(() => {
    hideChatRenameModal();
    setChat({} as IDialog);
  }, [hideChatRenameModal]);

  return {
    chatRenameLoading: patchLoading,
    initialChatName: chat?.name,
    onChatRenameOk,
    chatRenameVisible,
    hideChatRenameModal: handleHideModal,
    showChatRenameModal: handleShowChatRenameModal,
  };
};
