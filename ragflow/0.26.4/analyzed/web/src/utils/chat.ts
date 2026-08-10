/**
 * chat.ts — 对话工具：消息 UUID、LaTeX 预处理、引用标记与思考块折叠。
 */

import {
  ChatVariableEnabledField,
  EmptyConversationId,
} from '@/constants/chat';
import { IMessage, Message } from '@/interfaces/database/chat';
import { omit } from 'lodash';
import { v4 as uuid } from 'uuid';
import {
  citationMarkerReg,
  normalizeCitationDigits,
  parseCitationIndex,
} from './citation-utils';

/** 判断 conversationId 是否为有效非空 ID。 */
export const isConversationIdExist = (conversationId: string) => {
  return conversationId !== EmptyConversationId && conversationId !== '';
};

/** 为消息生成稳定 id：已有 id 则复用，否则 uuid。 */
export const buildMessageUuid = (message: Partial<Message | IMessage>) => {
  if ('id' in message && message.id) {
    return message.id;
  }
  return uuid();
};

/** 为消息列表补全 id 并剥离 reference 字段。 */
export const buildMessageListWithUuid = (messages?: Message[]) => {
  return (
    messages?.map((x: Message | IMessage) => ({
      ...omit(x, 'reference'),
      id: buildMessageUuid(x),
    })) ?? []
  );
};

/** 生成无连字符的新会话 ID。 */
export const generateConversationId = () => {
  return uuid().replace(/-/g, '');
};

// 渲染时为 id 加 role 前缀以保证列表 key 唯一
/** 返回 `${role}_${id}` 形式的 React key。 */
export const buildMessageUuidWithRole = (
  message: Partial<Message | IMessage>,
) => {
  return `${message.role}_${message.id}`;
};

// 预处理 LaTeX 分隔符以便 KaTeX / react-markdown 渲染
// ref: https://github.com/remarkjs/react-markdown/issues/785
//
// 定界符匹配：\] / \) 前非字母时才视为结束，避免 \right] 误截断
// are not part of a LaTeX command (e.g. \right], \big), \left)). Use a negative
// lookbehind (?<![a-zA-Z]) so that \] or \) preceded by a letter (command name)
// is not considered the closing delimiter. Use greedy matching so we match up to
// the last valid delimiter and avoid cutting at the first \] or \) inside the
// equation (e.g. \frac{1}{|y|} or \right]).

/** 块级公式 \[ ... \] 正则（负向后顾排除 LaTeX 命令名）。 */
const BLOCK_MATH_RE = /\\\[([\s\S]*?)(?<![a-zA-Z])\\\]/g;
/** 行内公式 \( ... \) 正则。 */
const INLINE_MATH_RE = /\\\(([\s\S]*?)(?<![a-zA-Z])\\\)/g;

/**
 * 将 \[\]/\(\) 转为 $$/$，并反转义 HTML 实体与双反斜杠。
 */
export const preprocessLaTeX = (content: string) => {
  const normalizedContent = content
    .replace(/\\\\\[/g, '\\[')
    .replace(/\\\\\(/g, '\\(')
    .replace(/\\\\\]/g, '\\]')
    .replace(/\\\\\)/g, '\\)')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&');

  const blockProcessedContent = normalizedContent.replace(
    BLOCK_MATH_RE,
    (_, equation) => `$$${equation}$$`,
  );

  const inlineProcessedContent = blockProcessedContent.replace(
    INLINE_MATH_RE,
    (_, equation) => `$${equation}$`,
  );

  return inlineProcessedContent;
};

/** 将 <think> 块折叠为可展开 details 区块。 */
export function replaceThinkToSection(text: string = '') {
  const pattern = /<think>([\s\S]*?)<\/think>/g;

  const result = text.replace(pattern, '<details class="think"><summary>Thinking...</summary>$1</details>');

  return result;
}

/** 将 <retrieving> 检索过程块折叠为 details。 */
export function replaceRetrievingToSection(text: string = '') {
  const pattern = /<retrieving>([\s\S]*?)<\/retrieving>/g;

  const result = text.replace(pattern, '<details class="retrieving"><summary>Retrieving...</summary>$1</details>');

  return result;
}

/** 聊天变量开关初始值：MaxTokens 默认关闭，其余开启。 */
export function setInitialChatVariableEnabledFieldValue(
  field: ChatVariableEnabledField,
) {
  return field !== ChatVariableEnabledField.MaxTokensEnabled;
}

/** 需在 UI 中展示图片预览的字段名。 */
const ShowImageFields = ['image', 'table'];

/** 判断字段是否应渲染图片/表格预览。 */
export function showImage(filed?: string) {
  return ShowImageFields.some((x) => x === filed);
}

/** 返回聊天设置页全部变量开关的初始映射。 */
export function setChatVariableEnabledFieldValuePage() {
  const variableCheckBoxFieldMap = Object.values(
    ChatVariableEnabledField,
  ).reduce<Record<string, boolean>>((pre, cur) => {
    pre[cur] = cur !== ChatVariableEnabledField.MaxTokensEnabled;
    return pre;
  }, {});

  return variableCheckBoxFieldMap;
}

/** 旧版引用标记正则（##数字##）。 */
const oldReg = /(#{2}[0-9\u0660-\u0669\u06F0-\u06F9]+\${2})/g;
/** 当前引用标记正则（来自 citation-utils）。 */
export const currentReg = citationMarkerReg;
export { normalizeCitationDigits, parseCitationIndex };

// 兼容旧版 ##index## 引用格式，转为 [ID:n]
/** 将旧版 ##数字## 引用替换为 [ID:数字]。 */
export const replaceTextByOldReg = (text: string) => {
  return text?.replace(oldReg, (substring: string) => {
    return `[ID:${substring.slice(2, -2)}]`;
  });
};
