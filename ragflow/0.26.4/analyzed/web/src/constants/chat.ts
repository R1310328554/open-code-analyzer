// chat.ts — 聊天模块常量：消息角色、路由参数、共享来源与数据集元数据模式。

/** 聊天消息发送方：助手或用户。 */
export enum MessageType {
  Assistant = 'assistant',
  User = 'user',
}

/** 对话变量表单中各 LLM 参数是否启用的布尔字段名。 */
export enum ChatVariableEnabledField {
  TemperatureEnabled = 'temperatureEnabled',
  TopPEnabled = 'topPEnabled',
  PresencePenaltyEnabled = 'presencePenaltyEnabled',
  FrequencyPenaltyEnabled = 'frequencyPenaltyEnabled',
  MaxTokensEnabled = 'maxTokensEnabled',
}

/** 启用字段 → 实际 LLM 参数字段名（temperature、top_p 等）。 */
export const variableEnabledFieldMap = {
  [ChatVariableEnabledField.TemperatureEnabled]: 'temperature',
  [ChatVariableEnabledField.TopPEnabled]: 'top_p',
  [ChatVariableEnabledField.PresencePenaltyEnabled]: 'presence_penalty',
  [ChatVariableEnabledField.FrequencyPenaltyEnabled]: 'frequency_penalty',
  [ChatVariableEnabledField.MaxTokensEnabled]: 'max_tokens',
};

/** 分享链接来源类型：Agent、Chat 或 Search。 */
export enum SharedFrom {
  Agent = 'agent',
  Chat = 'chat',
  Search = 'search',
}

/** 聊天页 URL 查询参数键：dialogId、conversationId、isNew。 */
export enum ChatSearchParams {
  DialogId = 'dialogId',
  ConversationId = 'conversationId',
  isNew = 'isNew',
}

/** 占位会话 ID，表示尚未创建真实 conversation。 */
export const EmptyConversationId = 'empty';

/** 知识库元数据提取模式：禁用、自动、半自动或手动。 */
export enum DatasetMetadata {
  Disabled = 'disabled',
  Automatic = 'auto',
  SemiAutomatic = 'semi_auto',
  Manual = 'manual',
}
