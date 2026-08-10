// document.ts — 文档实体与解析器配置类型（dataset 文档详情页）。

import { RunningStatus } from '@/constants/knowledge';

/** 单文档详情：解析进度、流水线、分块方法与元数据字段。 */
export interface IDocumentInfo {
  // chunk_num: number;
  create_date: string;
  create_time: number;
  created_by: string;
  nickname: string;
  id: string;
  dataset_id: string;
  location: string;
  name: string;
  parser_config: IParserConfig;
  // parser_id: string;
  pipeline_id: string;
  pipeline_name: string;
  process_begin_at?: string;
  process_duration: number;
  progress: number;
  progress_msg: string;
  run: RunningStatus;
  size: number;
  source_type: string;
  status: string;
  suffix: string;
  thumbnail: string;
  token_num: number;
  type: string;
  update_date: string;
  update_time: number;
  meta_fields?: Record<string, any>;
  chunk_method: string;
  chunk_count: number;
}

/** 文档级 parser_config：分块、Raptor/GraphRAG、MinerU 与元数据 schema。 */
export interface IParserConfig {
  delimiter?: string;
  html4excel?: boolean;
  layout_recognize?: string;
  pages?: any[];
  chunk_token_num?: number;
  auto_keywords?: number;
  auto_questions?: number;
  toc_extraction?: boolean;
  task_page_size?: number;
  raptor?: Raptor;
  graphrag?: GraphRag;
  image_context_window?: number;
  image_table_context_window?: number;
  image_context_size?: number;
  table_context_size?: number;
  mineru_parse_method?: 'auto' | 'txt' | 'ocr';
  mineru_formula_enable?: boolean;
  mineru_table_enable?: boolean;
  mineru_lang?: string;
  entity_types?: string[];
  metadata?: Array<{
    key?: string;
    description?: string;
    enum?: string[];
  }>;
  enable_metadata?: boolean;
}

/** 文档 Raptor 开关配置。 */
interface Raptor {
  use_raptor: boolean;
}

/** 文档 GraphRAG 配置：社区检测、实体类型与解析方法。 */
interface GraphRag {
  batch_chunk_token_size?: number;
  community?: boolean;
  entity_types?: string[];
  method?: string;
  resolution?: boolean;
  use_graphrag?: boolean;
}

/** 文档列表筛选项统计：运行状态、后缀与元数据分布。 */
export type IDocumentInfoFilter = {
  run_status: Record<number, number>;
  suffix: Record<string, number>;
  metadata: Record<string, Record<string, number>>;
};
