/**
 * next-chats/chat/interface.ts — 聊天页分段表单、变量表与 LLM 采样参数类型。
 */

import { FormInstance } from '@/interfaces/antd-compat';

/** 分段内容区：控制显隐、绑定 antd Form 与校验错误回调。 */
export interface ISegmentedContentProps {
  show: boolean;
  form: FormInstance;
  setHasError: (hasError: boolean) => void;
}

/** LLM 可调参数默认值（temperature、top_p、惩罚项、max_tokens）。 */
export interface IVariable {
  temperature: number;
  top_p: number;
  frequency_penalty: number;
  presence_penalty: number;
  max_tokens: number;
}

/** 提示词变量表格行：key、变量名与是否可选。 */
export interface VariableTableDataType {
  key: string;
  variable: string;
  optional: boolean;
}

/** 提交 prompt 配置时的变量参数（不含 variable 列展示字段）。 */
export type IPromptConfigParameters = Omit<VariableTableDataType, 'variable'>;
