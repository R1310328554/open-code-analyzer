// use-chat-url.ts — 聊天页 URL 查询参数（conversationId / isNew）读写与发消息前建会话。

import { ChatSearchParams } from '@/constants/chat';
import { useGetChatSearchParams } from '@/hooks/use-chat-request';
import { IMessage } from '@/interfaces/database/chat';
import { useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router';
import { useSetConversation } from './use-set-conversation';

/**
 * 统一管理聊天 URL 查询参数（conversationId 与 isNew）。
 * 替代 use-chat-request 中的 useClickConversationCard 与 use-set-chat-route 中的 useSetChatRouteParams。
 */
export const useChatUrlParams = () => {
  const [currentQueryParameters, setSearchParams] = useSearchParams();
  // 基于当前 URL 克隆一份可修改的查询参数对象
  const newQueryParameters: URLSearchParams = useMemo(
    () => new URLSearchParams(currentQueryParameters.toString()),
    [currentQueryParameters],
  );

  /** 仅更新 URL 中的 conversationId。 */
  const setConversationId = useCallback(
    (conversationId: string) => {
      newQueryParameters.set(ChatSearchParams.ConversationId, conversationId);
      setSearchParams(newQueryParameters);
    },
    [setSearchParams, newQueryParameters],
  );

  /** 更新 isNew 标记（新建会话时为 'true'）。 */
  const setIsNew = useCallback(
    (isNew: string) => {
      newQueryParameters.set(ChatSearchParams.isNew, isNew);
      setSearchParams(newQueryParameters);
    },
    [setSearchParams, newQueryParameters],
  );

  /** 读取当前 URL 中的 isNew 值。 */
  const getIsNew = useCallback(() => {
    return newQueryParameters.get(ChatSearchParams.isNew);
  }, [newQueryParameters]);

  /** 同时写入 conversationId 与 isNew 并刷新路由。 */
  const setConversationBoth = useCallback(
    (conversationId: string, isNew: string) => {
      newQueryParameters.set(ChatSearchParams.ConversationId, conversationId);
      newQueryParameters.set(ChatSearchParams.isNew, isNew);
      setSearchParams(newQueryParameters);
    },
    [setSearchParams, newQueryParameters],
  );

  return {
    setConversationId,
    setIsNew,
    getIsNew,
    setConversationBoth,
  };
};

/**
 * 发送首条消息前：若尚无会话或处于 isNew 状态，则先创建后端会话并同步 URL。
 */
export function useCreateConversationBeforeSendMessage() {
  const { conversationId, isNew } = useGetChatSearchParams();
  const { setConversation } = useSetConversation();
  const { setConversationBoth } = useChatUrlParams();

  // 无有效会话或标记为新建时，先调用 setConversation 创建后端记录
  const createConversationBeforeSendMessage = useCallback(
    async (value: string) => {
      let currentMessages: Array<IMessage> = [];
      if (conversationId === '' || isNew === 'true') {
        const data = await setConversation(value);
        if (!data || data.code !== 0) {
          return;
        }
        const backendConvId = data.data.id;
        setConversationBoth(backendConvId, '');
        currentMessages = data.data.messages;
        return {
          targetConversationId: backendConvId,
          currentMessages,
        };
      }

      return {
        targetConversationId: conversationId,
        currentMessages,
      };
    },
    [conversationId, isNew, setConversation, setConversationBoth],
  );

  return {
    createConversationBeforeSendMessage,
  };
}

/** createConversationBeforeSendMessage 函数类型。 */
export type CreateConversationBeforeSendMessageType = ReturnType<
  typeof useCreateConversationBeforeSendMessage
>['createConversationBeforeSendMessage'];

/** createConversationBeforeSendMessage 的异步返回值类型。 */
export type CreateConversationBeforeSendMessageReturnType = Awaited<
  ReturnType<CreateConversationBeforeSendMessageType>
>;
