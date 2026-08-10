/**
 * chat-channel-service.ts — 聊天渠道（Chat Channel）配置与运行时状态 API。
 */

import api from '@/utils/api';
import registerServer from '@/utils/register-server';
import request from '@/utils/request';

const { chatChannelSet, chatChannelList } = api;
/** 基础 CRUD 方法表：chatChannelSet / chatChannelList。 */
const methods = {
  chatChannelSet: {
    url: chatChannelSet,
    method: 'post',
  },
  chatChannelList: {
    url: chatChannelList,
    method: 'get',
  },
} as const;

/** 默认导出：registerServer 生成的聊天渠道 API 客户端。 */
const chatChannelService = registerServer<keyof typeof methods>(
  methods,
  request,
);

/** 获取单个聊天渠道详情。 */
export const fetchChatChannelDetail = (id: string) =>
  request.get(api.chatChannelDetail(id));

/** 部分更新聊天渠道配置。 */
export const updateChatChannel = (id: string, data: Record<string, any>) =>
  request.patch(api.chatChannelUpdate(id), { data });

/** 删除聊天渠道。 */
export const deleteChatChannel = (id: string) =>
  request.delete(api.chatChannelDel(id));

/** 查询聊天渠道运行时状态（连接/消息队列等）。 */
export const fetchChatChannelRuntime = (id: string) =>
  request.get(api.chatChannelRuntime(id));

export default chatChannelService;
