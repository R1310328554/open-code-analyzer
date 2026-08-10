// use-cache-chat-log.ts — 聊天 SSE 事件按 message_id 缓存，支持按类型过滤与去重追加。

import {
  IEventList,
  INodeEvent,
  MessageEventType,
} from '@/hooks/use-send-message';
import { get, isEmpty } from 'lodash';
import { useCallback, useMemo, useState } from 'react';
import { MessageWaitSuffix } from '../constant/chat';

/** 查询节点事件时需排除的 Message 类 SSE 类型。 */
export const ExcludeTypes = [
  MessageEventType.Message,
  MessageEventType.MessageEnd,
];

/** 去掉 messageId 末尾的等待后缀，归一化为真实消息 ID。 */
const resolveMessageId = (messageId: string) =>
  messageId?.replace(new RegExp(`${MessageWaitSuffix}$`), '');

/** 维护 messageId → 事件列表映射，并提供过滤、清空与当前消息上下文。 */
export function useCacheChatLog() {
  const [messageIdPool, setMessageIdPool] = useState<
    Record<string, IEventList>
  >({});

  const [latestTaskId, setLatestTaskId] = useState('');

  const [currentMessageId, setCurrentMessageId] = useState('');

  /** 按 messageId 返回该消息的全部缓存事件。 */
  const filterEventListByMessageId = useCallback(
    (messageId: string) => {
      const resolvedId = resolveMessageId(messageId);
      return messageIdPool[resolvedId]?.filter(
        (x) => x.message_id === resolvedId,
      );
    },
    [messageIdPool],
  );

  /** 在当前消息下按 event 类型筛选事件列表。 */
  const filterEventListByEventType = useCallback(
    (eventType: string) => {
      const resolvedId = resolveMessageId(currentMessageId);
      return messageIdPool[resolvedId]?.filter((x) => x.event === eventType);
    },
    [messageIdPool, currentMessageId],
  );

  /** 清空全部消息的缓存事件池。 */
  const clearEventList = useCallback(() => {
    setMessageIdPool({});
  }, []);

  /** 去重追加事件并更新 latestTaskId（取首条 task_id）。 */
  const addEventList = useCallback((events: IEventList, message_id: string) => {
    if (!isEmpty(events)) {
      const taskId = get(events, '0.task_id');
      setLatestTaskId(taskId);

      setMessageIdPool((prev) => {
        const list = [...(prev[message_id] ?? [])];

        events.forEach((event) => {
          if (!list.some((y) => y === event)) {
            list.push(event);
          }
        });

        return { ...prev, [message_id]: list };
      });
    }
  }, []);

  /** 当前消息的非 Message 类节点事件（用于画布 trace 展示）。 */
  const currentEventListWithoutMessage = useMemo(() => {
    const resolvedId = resolveMessageId(currentMessageId);
    const list = messageIdPool[resolvedId]?.filter(
      (x) =>
        x.message_id === resolvedId && ExcludeTypes.every((y) => y !== x.event),
    );
    return list as INodeEvent[];
  }, [currentMessageId, messageIdPool]);

  /** 按指定 messageId 返回排除 Message 类事件后的节点事件列表。 */
  const currentEventListWithoutMessageById = useCallback(
    (messageId: string) => {
      const resolvedId = resolveMessageId(messageId);
      const list = messageIdPool[resolvedId]?.filter(
        (x) =>
          x.message_id === resolvedId &&
          ExcludeTypes.every((y) => y !== x.event),
      );
      return list as INodeEvent[];
    },
    [messageIdPool],
  );

  return {
    currentEventListWithoutMessage,
    currentEventListWithoutMessageById,
    clearEventList,
    addEventList,
    filterEventListByEventType,
    filterEventListByMessageId,
    setCurrentMessageId,
    currentMessageId,
    latestTaskId,
  };
}
