// chat.ts — 聊天反馈与知识库问答请求体类型。

/** 消息反馈：点赞/点踩与可选文字 feedback。 */
export interface IFeedbackRequestBody {
  messageId?: string;
  thumbup?: boolean;
  feedback?: string;
}

/** 知识库问答：question、kb_ids 与可选 search_id。 */
export interface IAskRequestBody {
  question: string;
  kb_ids: string[];
  search_id?: string;
}
