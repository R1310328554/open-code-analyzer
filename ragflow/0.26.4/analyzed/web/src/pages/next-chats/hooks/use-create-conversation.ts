// use-create-conversation.ts — 上传文档前为 isNew 会话创建后端 conversation。

import { useCallback } from 'react';
import { useParams } from 'react-router';
import { useChatUrlParams } from './use-chat-url';
import { useSetConversation } from './use-set-conversation';

/** 在 isNew 会话中上传文档前先调用 setConversation 持久化会话。 */
export const useCreateConversationBeforeUploadDocument = () => {
  const { setConversation } = useSetConversation();
  const { id: dialogId } = useParams();
  const { getIsNew } = useChatUrlParams();

  /** 若 URL 标记 isNew，则以 message 为会话名创建记录并返回 API 响应。 */
  const createConversationBeforeUploadDocument = useCallback(
    async (message: string) => {
      const isNew = getIsNew();
      if (isNew === 'true') {
        const data = await setConversation(message);

        return data;
      }
    },
    [setConversation, getIsNew],
  );

  return {
    createConversationBeforeUploadDocument,
    dialogId,
  };
};
