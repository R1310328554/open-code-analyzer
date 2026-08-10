// constant.ts — Dataflow 结果页：分块文本模式、时间线节点类型与 URL 查询键。

/** 与知识库分块页一致的文本展示模式。 */
export enum ChunkTextMode {
  Full = 'full',
  Ellipse = 'ellipse',
}

/** Pipeline 各阶段在时间轴上的节点类型标识。 */
export enum TimelineNodeType {
  begin = 'file',
  parser = 'parser',
  contextGenerator = 'extractor',
  titleChunker = 'titleChunker',
  tokenChunker = 'tokenChunker',
  tokenizer = 'tokenizer',
  end = 'end',
}

/** 跳转 Dataflow 结果页时 URL searchParams 的键名常量。 */
export enum PipelineResultSearchParams {
  DocumentId = 'doc_id',
  KnowledgeId = 'knowledgeId',
  Type = 'type',
  IsReadOnly = 'is_read_only',
  AgentId = 'agent_id',
  AgentTitle = 'agent_title',
  /** 上传者标识 */
  CreatedBy = 'created_by', // Who uploaded the file
  DocumentExtension = 'extension',
}
