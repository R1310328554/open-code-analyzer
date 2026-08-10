// use-send-shared-message.ts — 外链/嵌入共享聊天：URL 参数解析与 bot completion。

import { NextMessageInputOnPressEnterParameter } from '@/components/message-input/next';
import message from '@/components/ui/message';
import { MessageType, SharedFrom } from '@/constants/chat';
import {
  useHandleMessageInputChange,
  useSelectDerivedMessages,
  useSendMessageWithSse,
} from '@/hooks/logic-hooks';
import { useFetchExternalChatInfo } from '@/hooks/use-chat-request';
import { Message } from '@/interfaces/database/chat';
import { get } from 'lodash';
import trim from 'lodash/trim';
import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';
import { v4 as uuid } from 'uuid';

/** 判断共享 completion 响应是否为 HTTP 或业务错误。 */
const isCompletionError = (res: any) =>
  res && (res?.response.status !== 200 || res?.data?.code !== 0);

/** 输入为空时禁用发送按钮。 */
export const useSendButtonDisabled = (value: string) => {
  return trim(value) === '';
};

/** 从 URL 解析共享聊天参数（from、shared_id、theme 及 data_* 自定义字段）。 */
export const useGetSharedChatSearchParams = () => {
  const [searchParams] = useSearchParams();
  // 以 data_ 为前缀的查询项映射为 data 对象
  const data = Object.fromEntries(
    Array.from(searchParams.entries())
      .filter(([key]) => key.startsWith(data_prefix))
      .map(([key, value]) => [key.replace(data_prefix, ''), value]),
  );
  return {
    from: searchParams.get('from') as SharedFrom,
    sharedId: searchParams.get('shared_id'),
    locale: searchParams.get('locale'),
    theme: searchParams.get('theme'),
    data: data,
    visibleAvatar: searchParams.get('visible_avatar')
      ? searchParams.get('visible_avatar') !== '1'
      : true,
  };
};

/** 共享页完整发送流程：初始化 session、SSE 问答与错误处理。 */
export const useSendSharedMessage = () => {
  const {
    from,
    sharedId: conversationId,
    data: data,
  } = useGetSharedChatSearchParams();
  const { handleInputChange, value, setValue } = useHandleMessageInputChange();
  // Agent 与 Chatbot 共用不同 completions 路径
  const completionUrl = `/api/v1/${from === SharedFrom.Agent ? 'agentbots' : 'chatbots'}/${conversationId}/completions`;
  const { data: chatInfo } = useFetchExternalChatInfo();
  const { send, answer, done, stopOutputMessage } = useSendMessageWithSse();
  const {
    derivedMessages,
    removeLatestMessage,
    addNewestAnswer,
    addNewestQuestion,
    scrollRef,
    messageContainerRef,
    removeAllMessages,
    removeAllMessagesExceptFirst,
  } = useSelectDerivedMessages();
  const [hasError, setHasError] = useState(false);

  /** 向共享 bot 发送单轮 question，失败时回滚 UI。 */
  const sendMessage = useCallback(
    async (
      message: Message,
      id?: string,
      enableThinking?: boolean,
      enableInternet?: boolean,
    ) => {
      const res = await send(completionUrl, {
        conversation_id: id ?? conversationId,
        quote: true,
        question: message.content,
        session_id: get(derivedMessages, '0.session_id'),
        reasoning: enableThinking,
        internet: enableInternet,
        ...(chatInfo?.llm_id ? { model_name: chatInfo.llm_id } : {}),
      });

      if (isCompletionError(res)) {
        // cancel loading
        setValue(message.content);
        removeLatestMessage();
      }
    },
    [
      send,
      completionUrl,
      conversationId,
      derivedMessages,
      setValue,
      removeLatestMessage,
      chatInfo,
    ],
  );

  const handleSendMessage = useCallback(
    async (
      message: Message,
      enableThinking?: boolean,
      enableInternet?: boolean,
    ) => {
      sendMessage(message, undefined, enableThinking, enableInternet);
    },
    [sendMessage],
  );

  /** 挂载时用空 question 换取 session_id，失败则展示错误。 */
  const fetchSessionId = useCallback(async () => {
    const payload = { question: '' };
    const ret = await send(completionUrl, { ...payload, ...data });
    if (isCompletionError(ret)) {
      message.error(ret?.data.message ?? 'Unknown error');
      setHasError(true);
    }
  }, [send, completionUrl]);

  useEffect(() => {
    fetchSessionId();
  }, [fetchSessionId]);

  useEffect(() => {
    if (answer.answer) {
      addNewestAnswer(answer);
    }
  }, [answer, addNewestAnswer]);

  /** 共享页回车：追加用户消息并触发 sendMessage。 */
  const handlePressEnter = useCallback(
    ({
      enableThinking,
      enableInternet,
    }: NextMessageInputOnPressEnterParameter) => {
      if (trim(value) === '') return;
      const id = uuid();
      if (done) {
        setValue('');
        addNewestQuestion({
          content: value,
          doc_ids: [],
          id,
          role: MessageType.User,
        });
        handleSendMessage(
          {
            content: value.trim(),
            id,
            role: MessageType.User,
          },
          enableThinking,
          enableInternet,
        );
      }
    },
    [addNewestQuestion, done, handleSendMessage, setValue, value],
  );

  return {
    handlePressEnter,
    handleInputChange,
    value,
    sendLoading: !done,
    loading: false,
    derivedMessages,
    hasError,
    stopOutputMessage,
    scrollRef,
    messageContainerRef,
    removeAllMessages,
    removeAllMessagesExceptFirst,
  };
};
