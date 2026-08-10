// use-set-conversation.ts — 封装 createSession，以路由 chatId 创建 named 会话。

import { useCreateSession } from '@/hooks/use-chat-request';
import { useCallback } from 'react';
import { useParams } from 'react-router';

/** 调用 createSession API，在 current dialog 下新建会话并返回响应。 */
export const useSetConversation = () => {
  const { id: chatId } = useParams();
  const { createSession } = useCreateSession();

  /** 以 name 为会话标题创建后端 session。 */
  const setConversation = useCallback(
    async (name: string) => {
      const data = await createSession({ chatId: chatId!, name });
      return data;
    },
    [createSession, chatId],
  );

  return { setConversation };
};
