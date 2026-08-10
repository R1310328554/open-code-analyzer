/**
 * memory-service.ts — Agent 长期记忆 CRUD、消息内容与状态管理 API。
 */

import api from '@/utils/api';
import request from '@/utils/next-request';
import { registerNextServer } from '@/utils/register-server';

const {
  createMemory,
  getMemoryList,
  deleteMemory,
  getMemoryDetail,
  updateMemorySetting,
  getMemoryConfig,
  deleteMemoryMessage,
  getMessageContent,
  updateMessageState,
  // getMemoryDetailShare,
} = api;
/** 记忆 REST 方法表（create/list/delete/config/message 等）。 */
const methods = {
  createMemory: {
    url: createMemory,
    method: 'post',
  },
  getMemoryList: {
    url: getMemoryList,
    method: 'get',
  },
  deleteMemory: { url: deleteMemory, method: 'delete' },
  getMemoryConfig: {
    url: getMemoryConfig,
    method: 'get',
  },
  deleteMemoryMessage: { url: deleteMemoryMessage, method: 'delete' },
  getMessageContent: { url: getMessageContent, method: 'get' },
  updateMessageState: { url: updateMessageState, method: 'put' },
} as const;
/** 默认导出：记忆 registerNextServer 客户端。 */
const memoryService = registerNextServer<keyof typeof methods>(methods);
/** 按 ID 更新记忆配置/设置。 */
export const updateMemoryById = (id: string, data: any) => {
  return request.put(updateMemorySetting(id), { ...data });
};
/** 按 ID 获取记忆详情（带查询参数）。 */
export const getMemoryDetailById = (id: string, data: any) => {
  return request.get(getMemoryDetail(id), { params: data });
};
export default memoryService;
