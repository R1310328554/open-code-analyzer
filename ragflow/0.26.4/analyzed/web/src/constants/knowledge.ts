// knowledge.ts — 知识库模块常量：路由键、解析/运行状态、模型类型与文档解析器。

/** 知识库子路由 segment：数据集、测试、配置、知识图谱。 */
export enum KnowledgeRouteKey {
  Dataset = 'dataset',
  Testing = 'testing',
  Configuration = 'configuration',
  KnowledgeGraph = 'knowledgeGraph',
}

/** 数据集详情页路由基础路径键。 */
export const DatasetBaseKey = 'dataset';

/** 文档/任务运行状态（字符串枚举，含后续操作提示注释）。 */
export enum RunningStatus {
  UNSTART = 'UNSTART', // need to run
  RUNNING = 'RUNNING', // need to cancel
  CANCEL = 'CANCEL', // need to refresh
  DONE = 'DONE', // need to refresh
  FAIL = 'FAIL', // need to refresh
  SCHEDULE = 'SCHEDULE',
}

/** 旧版数字字符串运行状态，与 RunningStatus 并存兼容。 */
export enum RunningStatusOld {
  UNSTART = '0', // need to run
  RUNNING = '1', // need to cancel
  CANCEL = '2', // need to refresh
  DONE = '3', // need to refresh
  FAIL = '4', // need to refresh
  SCHEDULE = '5',
}

/** 运行状态 → 界面展示文案（新旧枚举均映射）。 */
export const RunningStatusMap = {
  [RunningStatus.UNSTART]: 'Pending',
  [RunningStatus.RUNNING]: 'Running',
  [RunningStatus.CANCEL]: 'Cancel',
  [RunningStatus.DONE]: 'Success',
  [RunningStatus.FAIL]: 'Failed',
  [RunningStatus.SCHEDULE]: 'Schedule',

  [RunningStatusOld.UNSTART]: 'Pending',
  [RunningStatusOld.RUNNING]: 'Running',
  [RunningStatusOld.CANCEL]: 'Cancel',
  [RunningStatusOld.DONE]: 'Success',
  [RunningStatusOld.FAIL]: 'Failed',
  [RunningStatusOld.SCHEDULE]: 'Schedule',
};

/** LLM 参数预设档位：即兴、精确、平衡。 */
export enum ModelVariableType {
  Improvise = 'Improvise',
  Precise = 'Precise',
  Balance = 'Balance',
}

/** 各预设档位对应的 temperature、top_p、penalty 与 max_tokens 默认值。 */
export const settledModelVariableMap = {
  [ModelVariableType.Improvise]: {
    temperature: 0.8,
    top_p: 0.9,
    frequency_penalty: 0.1,
    presence_penalty: 0.1,
    max_tokens: 4096,
  },
  [ModelVariableType.Precise]: {
    temperature: 0.2,
    top_p: 0.75,
    frequency_penalty: 0.5,
    presence_penalty: 0.5,
    max_tokens: 4096,
  },
  [ModelVariableType.Balance]: {
    temperature: 0.5,
    top_p: 0.85,
    frequency_penalty: 0.3,
    presence_penalty: 0.2,
    max_tokens: 4096,
  },
};

/** 租户可配置的 LLM 能力类型：对话、嵌入、重排、TTS 等。 */
export enum LlmModelType {
  Embedding = 'embedding',
  Chat = 'chat',
  Image2text = 'image2text',
  Speech2text = 'speech2text',
  Rerank = 'rerank',
  TTS = 'tts',
  Ocr = 'ocr',
}

/** 知识库页 URL 查询参数：文档 id、知识库 id、类型。 */
export enum KnowledgeSearchParams {
  DocumentId = 'doc_id',
  KnowledgeId = 'id',
  Type = 'type',
}

/** 文档存储类型：虚拟或可视化。 */
export enum DocumentType {
  Virtual = 'virtual',
  Visual = 'visual',
}

/** 文档分块/解析策略：通用、QA、简历、表格、论文等。 */
export enum DocumentParserType {
  Naive = 'naive',
  Qa = 'qa',
  Resume = 'resume',
  Manual = 'manual',
  Table = 'table',
  Paper = 'paper',
  Book = 'book',
  Laws = 'laws',
  Presentation = 'presentation',
  Picture = 'picture',
  One = 'one',
  Audio = 'audio',
  Email = 'email',
  Tag = 'tag',
  KnowledgeGraph = 'knowledge_graph',
}

/** 标签重命名 DOM/表单元素 id。 */
export const TagRenameId = 'tagRename';

/** 解析管线类型：内置或 Pipeline。 */
export enum ParseType {
  BuiltIn = 1,
  Pipeline = 2,
}
