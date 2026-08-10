/**
 * memories/interface.ts — 记忆库模块 TypeScript 类型：列表、详情、LLM 与 memory_config 结构。
 */

/** 创建记忆库成功后返回的基础字段。 */
export interface CreateMemoryResponse {
  id: string;
  name: string;
  description: string;
}

/** 记忆库列表查询参数（分页、排序、owner 过滤等）。 */
export interface MemoryListParams {
  keywords?: string;
  parser_id?: string;
  page?: number;
  page_size?: number;
  orderby?: string;
  desc?: boolean;
  owner_ids?: string;
}
/** 记忆类型：原始 / 语义 /  episodic /  procedural。 */
export type MemoryType = 'raw' | 'semantic' | 'episodic' | 'procedural';
/** 记忆存储后端：表格或图结构。 */
export type StorageType = 'table' | 'graph';
export type Permissions = 'me' | 'team';
/** 遗忘策略：先进先出或最近最少使用。 */
export type ForgettingPolicy = 'FIFO' | 'LRU';
/** 新建记忆库表单所需的最小字段集。 */
export interface ICreateMemoryProps {
  name: string;
  memory_type: MemoryType[];
  embd_id: string;
  llm_id: string;
}
/** 列表/详情中的完整记忆库实体。 */
export interface IMemory extends ICreateMemoryProps {
  id: string;
  avatar: string;
  tenant_id: string;
  owner_name: string;
  storage_type: StorageType;
  permissions: Permissions;
  description: string;
  memory_size: number;
  forgetting_policy: ForgettingPolicy;
  temperature: string;
  system_prompt: string;
  user_prompt: string;
  create_date: string;
  create_time: number;
}
/** 记忆库列表 API 标准响应包装。 */
export interface MemoryListResponse {
  code: number;
  data: {
    memory_list: Array<IMemory>;
    total_count: number;
  };
  message: string;
}

export interface DeleteMemoryProps {
  memory_id: string;
}

export interface DeleteMemoryResponse {
  code: number;
  data: boolean;
  message: string;
}

export interface IllmSettingProps {
  llm_id: string;
  parameter: string;
  temperature?: number;
  top_p?: number;
  frequency_penalty?: number;
  presence_penalty?: number;
}
interface IllmSettingEnableProps {
  temperatureEnabled?: boolean;
  topPEnabled?: boolean;
  presencePenaltyEnabled?: boolean;
  frequencyPenaltyEnabled?: boolean;
}
/** 记忆库应用详情：含 memory_config（检索、RAG、LLM 等开关）。 */
export interface IMemoryAppDetailProps {
  avatar: any;
  created_by: string;
  description: string;
  id: string;
  name: string;
  /** 检索与对话相关配置（知识库 ID、rerank、图谱、元数据过滤等）。 */
  memory_config: {
    cross_languages: string[];
    doc_ids: string[];
    chat_id: string;
    highlight: boolean;
    kb_ids: string[];
    keyword: boolean;
    query_mindmap: boolean;
    related_memory: boolean;
    rerank_id: string;
    use_rerank?: boolean;
    similarity_threshold: number;
    summary: boolean;
    llm_setting: IllmSettingProps & IllmSettingEnableProps;
    top_k: number;
    use_kg: boolean;
    vector_similarity_weight: number;
    web_memory: boolean;
    chat_settingcross_languages: string[];
    meta_data_filter?: {
      method: string;
      manual: { key: string; op: string; value: string }[];
    };
  };
  tenant_id: string;
  update_time: number;
}

/** 单条记忆库详情 API 响应。 */
export interface MemoryDetailResponse {
  code: number;
  data: IMemoryAppDetailProps;
  message: string;
}

// export type IUpdateMemoryProps = Omit<IMemoryAppDetailProps, 'id'> & {
//   id: string;
// };
