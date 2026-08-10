// dataset.ts — 数据集/知识库相关类型：连接器、分块配置、检索测试与图谱。

// 数据集列表与详情 API 返回结构存在差异，此处类型为前端统一视图。

import { RunningStatus } from '@/constants/knowledge';
import { DataSourceKey } from '@/pages/user-setting/data-source/constant';

/** 数据集外部数据源连接器：状态、来源与是否自动解析。 */
export interface IConnector {
  id: string;
  name: string;
  status: RunningStatus;
  source: DataSourceKey;
  auto_parse?: '0' | '1';
}

/** 知识库/数据集实体：分块策略、嵌入模型、GraphRAG/Raptor 任务与权限等。 */
export interface IDataset {
  avatar?: string;
  chunk_count: number;
  chunk_method: string;
  create_date: string;
  create_time: number;
  created_by: string;
  description?: string;
  document_count: number;
  embedding_model: string;
  size?: number;
  graphrag_task_finish_at: string;
  graphrag_task_id: Nullable<string>;
  id: string;
  language: string;
  mindmap_task_finish_at: null;
  mindmap_task_id: Nullable<string>;
  name: string;
  nickname: string;
  pagerank: number;
  parser_config: Parserconfig;
  permission: string;
  pipeline_id: string;
  raptor_task_finish_at: string;
  raptor_task_id: string;
  similarity_threshold: number;
  status: string;
  tenant_avatar: string;
  tenant_embd_id: number;
  tenant_id: string;
  token_num: number;
  update_date: string;
  update_time: number;
  vector_similarity_weight: number;
  connectors: IConnector[];
}

/** 数据集级解析器配置：分块、GraphRAG、Raptor、父子块与元数据开关。 */
interface Parserconfig {
  auto_keywords: number;
  auto_questions: number;
  children_delimiter: string;
  chunk_token_num: number;
  delimiter: string;
  from_page?: number;
  to_page?: number;
  graphrag: Graphrag;
  html4excel: boolean;
  image_context_size: number;
  layout_recognize: string;
  llm_id: string;
  metadata?: any;
  built_in_metadata?: Array<{ key: string; type: string }>;
  enable_metadata?: boolean;
  parent_child: Parentchild;
  raptor: Raptor;
  table_context_size: number;
  tag_kb_ids?: string[];
  topn_tags: number;
}

/** Raptor 层次聚类摘要配置：聚类方法、树构建器与阈值等。 */
interface Raptor {
  clustering_method?: 'gmm' | 'ahc';
  ext?: { clustering_method: 'gmm' | 'ahc'; tree_builder: 'raptor' | 'psi' };
  max_cluster: number;
  max_token: number;
  prompt: string;
  random_seed: number;
  threshold: number;
  tree_builder?: 'raptor' | 'psi';
  use_raptor: boolean;
}

/** 父子块分块策略：子块分隔符与是否启用 parent-child。 */
interface Parentchild {
  children_delimiter: string;
  use_parent_child: boolean;
}

/** GraphRAG 配置：实体类型、方法与是否启用。 */
interface Graphrag {
  batch_chunk_token_size?: number;
  entity_types: string[];
  method: string;
  use_graphrag: boolean;
}

/** 分页数据集列表 API 响应：kbs 数组与 total_datasets。 */
export interface IDatasetListResult {
  kbs: IDataset[];
  total_datasets: number;
}

// 自 knowledge.ts 迁移的类型定义

/** 知识库文件解析配置：分块 token 数、版面识别与页范围。 */
export interface IKnowledgeFileParserConfig {
  chunk_token_num: number;
  layout_recognize: boolean;
  pages: number[][];
  task_page_size: number;
}

/** 知识库内文档文件：解析进度、状态、分块数与 parser 配置。 */
export interface IKnowledgeFile {
  chunk_num: number;
  create_date: string;
  create_time: number;
  created_by: string;
  id: string;
  kb_id: string;
  location: string;
  name: string;
  parser_id: string;
  process_begin_at?: any;
  process_duration: number;
  progress: number; // 解析进度 0–1
  progress_msg: string; // 解析日志
  run: RunningStatus; // 解析运行状态
  size: number;
  source_type: string;
  status: string; // 是否启用
  thumbnail?: any; // 缩略图 base64
  token_num: number;
  type: string;
  update_date: string;
  update_time: number;
  parser_config: IKnowledgeFileParserConfig;
}

/** 租户模型与角色信息：各模态默认 LLM/嵌入/ASR/TTS 实例 ID。 */
export interface ITenantInfo {
  asr_id: string;
  embd_id: string;
  img2txt_id: string;
  llm_id: string;
  name: string;
  parser_ids: string;
  role: string;
  tenant_id: string;
  chat_id: string;
  speech2text_id: string;
  rerank_id?: string;
  tts_id: string;
  // 租户级模型实例 ID（覆盖用户默认）
  tenant_asr_id?: string;
  tenant_embd_id?: string;
  tenant_img2txt_id?: string;
  tenant_llm_id?: string;
  tenant_rerank_id?: string;
  tenant_tts_id?: string;
}

/** Chunk 所属文档内容类型：图片、表格或纯文本。 */
export type ChunkDocType = 'image' | 'table' | 'text';

/** 知识库 Chunk：内容、位置、关键词/标签与启用状态。 */
export interface IChunk {
  available_int: number; // 是否启用：0 禁用，1 启用
  chunk_id: string;
  content_with_weight: string;
  doc_id: string;
  doc_name: string;
  doc_type_kwd?: ChunkDocType;
  image_id: string;
  important_kwd?: string[];
  question_kwd?: string[]; // 问题关键词
  tag_kwd?: string[];
  positions: number[][];
  tag_feas?: Record<string, number>;
}

/** 检索测试单条 Chunk 命中：相似度、向量与高亮片段。 */
export interface ITestingChunk {
  chunk_id: string;
  content_ltks: string;
  content_with_weight: string;
  doc_id: string;
  doc_name: string;
  img_id: string;
  image_id: string;
  important_kwd: any[];
  kb_id: string;
  similarity: number;
  term_similarity: number;
  vector: number[];
  vector_similarity: number;
  highlight: string;
  positions: number[][];
  docnm_kwd: string;
  doc_type_kwd: string;
}

/** 检索测试文档聚合：doc_id、名称与命中 Chunk 数。 */
export interface ITestingDocument {
  count: number;
  doc_id: string;
  doc_name: string;
}

/** 检索测试结果：chunks、documents 与 labels 统计。 */
export interface ITestingResult {
  chunks: ITestingChunk[];
  documents: ITestingDocument[];
  total: number;
  labels?: Record<string, number>;
}

/** 新版检索测试响应：doc_aggs 聚合与 isRuned 标记。 */
export interface INextTestingResult {
  chunks: ITestingChunk[];
  doc_aggs: ITestingDocument[];
  total: number;
  labels?: Record<string, number>;
  isRuned?: boolean;
}

/** 标签重命名映射：原标签 → 新标签。 */
export type IRenameTag = { fromTag: string; toTag: string };

/** 知识图谱与思维导图数据：graph 邻接与 G6 TreeData。 */
export interface IKnowledgeGraph {
  graph: Record<string, any>;
  mind_map: import('@antv/g6/lib/types').TreeData;
}
