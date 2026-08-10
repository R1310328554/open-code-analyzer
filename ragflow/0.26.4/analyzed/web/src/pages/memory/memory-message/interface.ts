/**
 * memory-message/interface.ts — 记忆消息行、表格分页与内容预览的类型定义。
 */

/** 单条记忆消息：类型、来源、Agent、有效期与关联解析 task。 */
export interface IMessageInfo {
  message_id: number;
  message_type: 'semantic' | 'raw' | 'procedural';
  source_id: string | '-';
  user_id: string;
  agent_id: string;
  agent_name: string;
  session_id: string;
  valid_at: string;
  invalid_at: string;
  forget_at: string;
  status: boolean;
  /** 子消息抽取链（递归时排除 task 字段避免循环）。 */
  extract?: Omit<IMessageInfo, 'task'>[];
  /** 关联文档解析任务进度与 chunk 信息。 */
  task: {
    chunk_ids: string;
    create_time: number;
    digest: string;
    doc_id: string;
    from_page: number;
    id: string;
    progress: number;
    progress_msg: string;
  };
}

/** 消息列表 API 返回：messages 分页与 storage_type。 */
export interface IMessageTableProps {
  messages: { message_list: Array<IMessageInfo>; total_count: number };
  storage_type: string;
}

/** 消息正文与 embedding 文本，供详情弹窗展示。 */
export interface IMessageContentProps {
  content: string;
  content_embed: string;
}
