/**
 * chat-channel/interface.ts — 聊天渠道类型：展示信息、实例与配置结构。
 */

import { ChatChannelKey } from './constant';

/** 渠道类型卡片：名称、描述与图标（WhatsApp/Telegram 等）。 */
export interface IChatChannelInfo {
  id: ChatChannelKey;
  name: string;
  description: string;
  icon: React.ReactNode;
}

/** 用户已配置的渠道实例摘要。 */
export interface IChatChannelBase {
  id: string;
  name: string;
  channel: ChatChannelKey;
  // 列表接口联表返回的已绑定助手 ID 与名称
  chat_id?: string | null;
  dialog_name?: string | null;
}

/** 渠道详情：含租户、状态与 JSON config。 */
export type IChatChannel = IChatChannelBase & {
  config: Record<string, any>;
  status: string;
  tenant_id: string;
  create_date?: string;
  update_date?: string;
};

interface IChatChannelInfoItem {
  name: string;
  description: string;
  icon: JSX.Element;
}

/** 各 ChatChannelKey 对应的展示元数据映射。 */
export type IChatChannelInfoMap = Record<ChatChannelKey, IChatChannelInfoItem>;
