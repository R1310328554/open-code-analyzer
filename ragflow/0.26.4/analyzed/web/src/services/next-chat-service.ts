/**
 * next-chat-service.ts — 新版聊天应用 CRUD、会话/消息管理及 TTS/思维导图/相关问题 API。
 */

import api from '@/utils/api';
import { registerNextServer } from '@/utils/register-server';

const {
  createChat,
  listChats,
  getChat,
  updateChat,
  patchChat,
  deleteChat,
  bulkDeleteChats,
  createSession,
  listSessions,
  getSession,
  updateSession,
  removeSessions,
  deleteMessage,
  thumbup,
  chatsTts,
  chatsMindmap,
  chatsRelatedQuestions,
  documentInfoUpload,
  fetchExternalChatInfo,
} = api;

/** 聊天 REST 方法表：chat/session/message 及 TTS/mindmap 等扩展。 */
const methods = {
  createChat: {
    url: createChat,
    method: 'post',
  },
  listChats: {
    url: listChats,
    method: 'get',
  },
  getChat: {
    url: getChat,
    method: 'get',
  },
  updateChat: {
    url: updateChat,
    method: 'put',
  },
  patchChat: {
    url: patchChat,
    method: 'patch',
  },
  deleteChat: {
    url: deleteChat,
    method: 'delete',
  },
  bulkDeleteChats: {
    url: bulkDeleteChats,
    method: 'delete',
  },
  createSession: {
    url: createSession,
    method: 'post',
  },
  listSessions: {
    url: listSessions,
    method: 'get',
  },
  getSession: {
    url: getSession,
    method: 'get',
  },
  updateSession: {
    url: updateSession,
    method: 'patch',
  },
  removeSessions: {
    url: removeSessions,
    method: 'delete',
  },
  deleteMessage: {
    url: deleteMessage,
    method: 'delete',
  },
  thumbup: {
    url: thumbup,
    method: 'put',
  },
  chatsTts: {
    url: chatsTts,
    method: 'post',
  },
  chatsMindmap: {
    url: chatsMindmap,
    method: 'post',
  },
  chatsRelatedQuestions: {
    url: chatsRelatedQuestions,
    method: 'post',
  },
  documentInfoUpload: {
    method: 'post',
    url: documentInfoUpload,
  },
  fetchExternalChatInfo: {
    url: fetchExternalChatInfo,
    method: 'get',
  },
} as const;

/** 默认导出：新版聊天 API 客户端。 */
const chatService = registerNextServer<keyof typeof methods>(methods);

export default chatService;
