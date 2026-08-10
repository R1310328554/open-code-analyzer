// use-send-single-message.ts — 单聊天框发送 hook，供多框对比调试等场景复用。

import { NextMessageInputOnPressEnterParameter } from '@/components/message-input/next';
import { MessageType } from '@/constants/chat';
import {
  useHandleMessageInputChange,
  useSelectDerivedMessages,
  useSendMessageWithSse,
} from '@/hooks/logic-hooks';
import { useGetChatSearchParams } from '@/hooks/use-chat-request';
import { IMessage } from '@/interfaces/database/chat';
import api from '@/utils/api';
import { useCallback, useEffect } from 'react';
import { useParams } from 'react-router';
import { v4 as uuid } from 'uuid';
import { CreateConversationBeforeSendMessageReturnType } from './use-chat-url';
import { useUploadFile } from './use-upload-file';

/** useSendSingleMessage 入参：controller、输入值与附件状态。 */
export type UseSendSingleMessageParameter = {
  controller: AbortController;
} & Pick<ReturnType<typeof useHandleMessageInputChange>, 'value' | 'setValue'> &
  Pick<ReturnType<typeof useUploadFile>, 'files' | 'clearFiles'>;

/**
 * 与 useSendMessage 类似的发送逻辑，但会话 ID 由调用方传入（多框并行）。
 */
export function useSendSingleMessage({
  controller,
  value,
  setValue,
  files,
  clearFiles,
}: {
  controller: AbortController;
} & Pick<ReturnType<typeof useHandleMessageInputChange>, 'value' | 'setValue'> &
  Pick<ReturnType<typeof useUploadFile>, 'files' | 'clearFiles'>) {
  const { conversationId } = useGetChatSearchParams();
  const { id: chatId } = useParams();

  const { send, answer, done } = useSendMessageWithSse();

  const {
    scrollRef,
    messageContainerRef,
    setDerivedMessages,
    derivedMessages,
    addNewestAnswer,
    addNewestQuestion,
    removeLatestMessage,
    removeMessageById,
    removeMessagesAfterCurrentMessage,
  } = useSelectDerivedMessages();

  useEffect(() => {
    if (answer.answer) {
      addNewestAnswer(answer);
    }
  }, [answer, addNewestAnswer]);

  /** SSE completion；失败时恢复输入并移除最新占位消息。 */
  const sendMessage = useCallback(
    async ({
      message,
      currentConversationId,
      messages,
      enableInternet,
      enableThinking,
      ...params
    }: {
      message: IMessage;
      currentConversationId?: string;
      messages?: IMessage[];
    } & NextMessageInputOnPressEnterParameter) => {
      const sessionId = currentConversationId ?? conversationId;
      const res = await send(
        api.completionUrl,
        {
          chat_id: chatId,
          session_id: sessionId,
          messages: [
            ...(Array.isArray(messages) && messages?.length > 0
              ? messages
              : (derivedMessages ?? [])),
            message,
          ],
          reasoning: enableThinking,
          internet: enableInternet,
          ...params,
          pass_all_history_messages: true,
        },
        controller,
      );

      if (res && (res?.response.status !== 200 || res?.data?.code !== 0)) {
        // cancel loading
        setValue(message.content);
        console.info('removeLatestMessage111');
        removeLatestMessage();
      }
    },
    [
      derivedMessages,
      conversationId,
      chatId,
      removeLatestMessage,
      setValue,
      send,
      controller,
    ],
  );

  /** 由外层已创建会话后调用：追加问题并发送，参数含 targetConversationId。 */
  const handlePressEnter = useCallback(
    async ({
      enableThinking,
      enableInternet,
      currentMessages,
      targetConversationId,
      ...params
    }: NextMessageInputOnPressEnterParameter &
      CreateConversationBeforeSendMessageReturnType) => {
      const id = uuid();

      addNewestQuestion({
        content: value,
        files: files,
        id,
        role: MessageType.User,
        conversationId: targetConversationId,
      });

      if (done) {
        setValue('');
        sendMessage({
          currentConversationId: targetConversationId,
          messages: currentMessages,
          message: {
            id,
            content: value.trim(),
            role: MessageType.User,
            files: files,
            conversationId: targetConversationId,
          },
          enableInternet,
          enableThinking,
          ...params,
        });
      }
      clearFiles();
    },
    [addNewestQuestion, value, files, done, clearFiles, setValue, sendMessage],
  );

  return {
    scrollRef,
    messageContainerRef,
    setDerivedMessages,
    derivedMessages,
    addNewestAnswer,
    addNewestQuestion,
    removeLatestMessage,
    removeMessageById,
    removeMessagesAfterCurrentMessage,
    handlePressEnter,
    sendLoading: !done,
  };
}

/** handlePressEnter 函数类型，供父组件组合多个聊天框。 */
export type HandlePressEnterType = ReturnType<
  typeof useSendSingleMessage
>['handlePressEnter'];
