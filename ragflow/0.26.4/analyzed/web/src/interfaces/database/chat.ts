// database/chat.ts — 对话、消息、引用块与 Dialog 配置类型定义。

import { MessageType } from '@/constants/chat';
import { IAttachment } from '@/hooks/use-send-message';

export interface IDocumentDownloadInfo {
  doc_id: string;
  filename: string;
  mime_type: string;
  size?: number;
}

/** Dialog 提示词与 RAG/推理/TTS 等行为开关配置。 */
export interface PromptConfig {
  empty_response: string;
  parameters: Parameter[];
  prologue: string;
  system: string;
  tts?: boolean;
  quote: boolean;
  keyword: boolean;
  refine_multiturn: boolean;
  use_kg: boolean;
  reasoning?: boolean;
  cross_languages?: Array<string>;
  tavily_api_key?: string;
  toc_enhance?: boolean;
  reference_metadata?: {
    include?: boolean;
    fields?: string[];
  };
}

export interface Parameter {
  key: string;
  optional: boolean;
}

export interface LlmSetting {
  Creative: Variable;
  Custom: Variable;
  Evenly: Variable;
  Precise: Variable;
}

export interface Variable {
  frequency_penalty?: number;
  max_tokens?: number;
  presence_penalty?: number;
  temperature?: number;
  top_p?: number;
  tenant_llm_id?: string;
  model_type?: string;
}

/** 聊天助手（Dialog）实体：关联知识库、LLM 与 prompt_config。 */
export interface IDialog {
  create_date: string;
  create_time: number;
  description: string;
  icon: string;
  id: string;
  dialog_id?: string;
  dataset_ids: string[];
  kb_names: string[];
  language: string;
  llm_id: string;
  tenant_llm_id?: string;
  llm_setting: Variable;
  llm_setting_type?: string;
  name: string;
  prompt_config: PromptConfig;
  prompt_type: string;
  status: string;
  tenant_id: string;
  update_date: string;
  update_time: number;
  vector_similarity_weight: number;
  similarity_threshold: number;
  top_k: number;
  top_n: number;
  rerank_id?: string;
  meta_data_filter: MetaDataFilter;
}

interface MetaDataFilter {
  manual: Manual[];
  method: string;
}

interface Manual {
  key: string;
  op: string;
  value: string;
}

/** 会话实体：messages、reference 与 chat_id。 */
export interface IConversation {
  create_date: string;
  create_time: number;
  chat_id: string;
  id: string;
  avatar: string;
  messages: Message[];
  reference: IReference[];
  name: string;
  update_date: string;
  update_time: number;
  is_new: true;
}

/** 单条消息：content、role、附件与引用等。 */
export interface Message {
  content: string;
  role: MessageType;
  doc_ids?: string[];
  prompt?: string;
  id?: string;
  audio_binary?: string;
  data?: any;
  files?: (File | UploadResponseDataType)[];
  chatBoxId?: string;
  attachment?: IAttachment;
  downloads?: IDocumentDownloadInfo[];
}

/** 检索引用分块：相似度、文档名与 bbox positions。 */
export interface IReferenceChunk {
  id: string;
  content: null;
  document_id: string;
  document_name: string;
  dataset_id: string;
  image_id: string;
  similarity: number;
  vector_similarity: number;
  term_similarity: number;
  positions: number[];
  doc_type?: string;
  document_metadata?: Record<string, any>;
}

/** 引用汇总：chunks 数组、doc_aggs 与 total。 */
export interface IReference {
  chunks: IReferenceChunk[];
  doc_aggs: Docagg[];
  total: number;
}

export interface IReferenceObject {
  chunks: Record<string, IReferenceChunk>;
  doc_aggs: Record<string, Docagg>;
}

/** 前端展示用助手回复，含 reference 与 attachment。 */
export interface IAnswer {
  answer: string;
  attachment?: IAttachment;
  downloads?: IDocumentDownloadInfo[];
  reference?: IReference;
  conversationId?: string;
  prompt?: string;
  id?: string;
  audio_binary?: string;
  data?: any;
  chatBoxId?: string;
}

export interface Docagg {
  count: number;
  doc_id: string;
  doc_name: string;
  url?: string;
}

// interface Chunk {
//   chunk_id: string;
//   content_ltks: string;
//   content_with_weight: string;
//   doc_id: string;
//   docnm_kwd: string;
//   img_id: string;
//   important_kwd: any[];
//   kb_id: string;
//   similarity: number;
//   term_similarity: number;
//   vector_similarity: number;
// }

export interface IToken {
  create_date: string;
  create_time: number;
  tenant_id: string;
  token: string;
  update_date?: any;
  update_time?: any;
  beta: string;
}

/** 对话统计时序：pv/uv/speed/tokens/round/thumb_up。 */
export interface IStats {
  pv: [string, number][];
  uv: [string, number][];
  speed: [string, number][];
  tokens: [string, number][];
  round: [string, number][];
  thumb_up: [string, number][];
}

/** 外链/embed 聊天页展示的标题、开场白与 LLM 信息。 */
export interface IExternalChatInfo {
  avatar?: string;
  title: string;
  prologue?: string;
  has_tavily_key?: boolean;
  llm_id?: string;
}

export interface IMessage extends Message {
  id: string;
  reference?: IReference; // the latest news has reference
  conversationId?: string; // To distinguish which conversation the message belongs to
}

/** 客户端会话，messages 为带 id 的 IMessage。 */
export interface IClientConversation extends IConversation {
  messages: IMessage[];
}

export interface UploadResponseDataType {
  created_at: number;
  created_by: string;
  extension: string;
  id: string;
  mime_type: string;
  name: string;
  preview_url: null;
  size: number;
}
