// knowledge.ts — 知识库检索测试、列表与文档筛选请求体类型。

/** 知识库检索测试：问题、相似度阈值、rerank、top_k 与元数据过滤。 */
export interface ITestRetrievalRequestBody {
  question: string;
  similarity_threshold: number;
  vector_similarity_weight: number;
  rerank_id?: string;
  top_k?: number;
  use_kg?: boolean;
  highlight?: boolean;
  kb_id?: string[];
  meta_data_filter?: {
    logic?: string;
    method?: string;
    manual?: Array<{
      key: string;
      op: string;
      value: string;
    }>;
    semi_auto?: string[];
  };
}

/** 知识库列表 POST 体：按 owner_ids 筛选。 */
export interface IFetchKnowledgeListRequestBody {
  owner_ids?: string[];
}

/** 知识库列表查询参数：分页、keywords、parser_id 与 owner_ids。 */
export interface IFetchKnowledgeListRequestParams {
  id?: string;
  page?: number;
  page_size?: number;
  ext?: {
    keywords?: string;
    owner_ids?: string[];
    parser_id?: string;
  };
}

/** 文档列表筛选：后缀、运行状态、空元数据标记与 metadata 分布。 */
export interface IFetchDocumentListRequestBody {
  suffix?: string[];
  run_status?: string[];
  return_empty_metadata?: boolean;
  metadata?: Record<string, string[]>;
}
