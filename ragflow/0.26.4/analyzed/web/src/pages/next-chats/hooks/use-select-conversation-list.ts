// use-select-conversation-list.ts — 会话列表派生、开场白与临时新会话插入。

import { MessageType } from '@/constants/chat';
import { useTranslate } from '@/hooks/common-hooks';
import {
  useFetchChatList,
  useFetchSessionList,
} from '@/hooks/use-chat-request';
import { IConversation } from '@/interfaces/database/chat';
import { generateConversationId } from '@/utils/chat';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router';
import { useChatUrlParams } from './use-chat-url';

/** 从当前 dialog 的聊天列表中读取 prompt_config.prologue 作为开场白。 */
export const useFindPrologueFromDialogList = () => {
  const { id: dialogId } = useParams();
  const { data } = useFetchChatList();

  const prologue = useMemo(() => {
    return data.chats.find((x) => x.id === dialogId)?.prompt_config?.prologue;
  }, [dialogId, data]);

  return prologue;
};

/** 合并服务端会话列表与本地临时新会话，支持搜索与新建占位卡片。 */
export const useSelectDerivedConversationList = () => {
  const { t } = useTranslate('chat');

  const [list, setList] = useState<Array<IConversation>>([]);
  const {
    data: conversationList,
    loading,
    handleInputChange,
    searchString,
  } = useFetchSessionList();

  const { id: dialogId } = useParams();
  const prologue = useFindPrologueFromDialogList();
  const { setConversationBoth } = useChatUrlParams();

  /** 在列表顶部插入 is_new 临时会话并更新 URL。 */
  const addTemporaryConversation = useCallback(() => {
    const conversationId = generateConversationId();
    setList((pre) => {
      if (dialogId) {
        setConversationBoth(conversationId, 'true');
        const nextList = [
          {
            id: conversationId,
            name: t('newConversation'),
            chat_id: dialogId,
            is_new: true,
            messages: [
              {
                content: prologue,
                role: MessageType.Assistant,
              },
            ],
          } as any,
          ...conversationList,
        ];
        return nextList;
      }

      return pre;
    });
  }, [dialogId, setConversationBoth, t, prologue, conversationList]);

  /** 取消新建或删除失败时，从本地列表移除临时会话。 */
  const removeTemporaryConversation = useCallback((conversationId: string) => {
    setList((prevList) => {
      return prevList.filter(
        (conversation) => conversation.id !== conversationId,
      );
    });
  }, []);

  // 服务端列表变更时同步到本地 list 状态

  useEffect(() => {
    setList([...conversationList]);
  }, [conversationList]);

  return {
    list,
    addTemporaryConversation,
    removeTemporaryConversation,
    loading,
    handleInputChange,
    searchString,
  };
};
