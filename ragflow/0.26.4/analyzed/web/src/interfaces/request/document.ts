// document.ts — 文档解析器变更与元数据更新请求体。

/** 更新 parser_config 的可选字段：分块、Raptor、元数据等。 */
export interface IChangeParserConfigRequestBody {
  pages?: number[][];
  chunk_token_num?: number;
  layout_recognize?: string;
  task_page_size?: number;
  delimiter?: string;
  auto_keywords?: number;
  auto_questions?: number;
  html4excel?: boolean;
  toc_extraction?: boolean;
  image_table_context_window?: number;
  image_context_size?: number;
  table_context_size?: number;
  raptor?: {
    use_raptor?: boolean;
    prompt?: string;
    max_token?: number;
    threshold?: number;
    max_cluster?: number;
    random_seed?: number;
    scope?: string;
    clustering_method?: 'gmm' | 'ahc';
    tree_builder?: 'raptor' | 'psi';
  };
  // 元数据 schema 相关字段
  metadata?: Array<{
    key?: string;
    description?: string;
    enum?: string[];
  }>;
  built_in_metadata?: Array<{
    key?: string;
    description?: string;
    enum?: string[];
  }>;
  enable_metadata?: boolean;
}

/** 切换文档解析器/流水线：parser_id、pipeline_id 与新 parser_config。 */
export interface IChangeParserRequestBody {
  parser_id: string;
  pipeline_id?: string;
  doc_id?: string;
  parser_config: IChangeParserConfigRequestBody;
}

/** 设置文档自定义 meta：documentId 与 JSON 字符串 meta。 */
export interface IDocumentMetaRequestBody {
  documentId: string;
  meta: string; // JSON 格式字符串
}
