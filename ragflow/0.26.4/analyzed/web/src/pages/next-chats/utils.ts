// utils.ts — 新版聊天页会话 ID 校验与消息引用（reference）聚合工具。

import { EmptyConversationId, MessageType } from '@/constants/chat';
import {
  IConversation,
  IMessage,
  IReference,
} from '@/interfaces/database/chat';
import { isEmpty } from 'lodash';

/** 判断 conversationId 是否为有效非空 ID（非 empty 占位）。 */
export const isConversationIdExist = (conversationId: string) => {
  return conversationId !== EmptyConversationId && conversationId !== '';
};

/** 从会话 reference 去重收集 doc_aggs 中的 doc_id，逗号拼接。 */
export const getDocumentIdsFromConversionReference = (data: IConversation) => {
  const documentIds = data.reference.reduce(
    (pre: Array<string>, cur: IReference) => {
      cur.doc_aggs
        ?.map((x) => x.doc_id)
        .forEach((x) => {
          if (pre.every((y) => y !== x)) {
            pre.push(x);
          }
        });
      return pre;
    },
    [],
  );
  return documentIds.join(',');
};

/**
 * 为单条助手消息解析引用块：优先 message.reference，否则按助手消息序号索引 conversation.reference。
 */
export const buildMessageItemReference = (
  conversation: { messages: IMessage[]; reference: IReference[] },
  message: IMessage,
) => {
  const assistantMessages = conversation.messages
    ?.filter(
      (x) =>
        x.role === MessageType.Assistant && !x.content.startsWith('**ERROR**:'), // 排除 ERROR 占位消息
    )
    .slice(1);
  const referenceIndex = assistantMessages.findIndex(
    (x) => x.id === message.id,
  );
  const reference = !isEmpty(message?.reference)
    ? message?.reference
    : (conversation?.reference ?? [])[referenceIndex];

  return reference ?? { doc_aggs: [], chunks: [], total: 0 };
};
