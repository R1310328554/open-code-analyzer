// dataset-common.ts — 数据集概览页公共枚举：日志 Tab 与后处理类型映射。

/** 概览页日志视图 Tab：文件级日志与数据集级日志。 */
export enum LogTabs {
  FILE_LOGS = 'fileLogs',
  DATASET_LOGS = 'datasetLogs',
}

/** 知识库后处理任务类型：知识图谱 GraphRAG 与 RAPTOR 聚类。 */
export enum ProcessingType {
  knowledgeGraph = 'Graph',
  raptor = 'RAPTOR',
}

/** ProcessingType 到 UI 展示文案的映射，含 GraphRAG 别名。 */
export const ProcessingTypeMap = {
  [ProcessingType.knowledgeGraph]: 'Knowledge Graph',
  [ProcessingType.raptor]: 'RAPTOR',
  GraphRAG: 'Knowledge Graph',
};
