// interface.ts — 数据集概览页类型：日志表格 props、汇总统计与单条日志项。

import { RunningStatus, RunningStatusMap } from '../dataset/constant';
import { LogTabs } from './dataset-common';

/** 文档日志行附加字段：文件名与 RunningStatus 展示名。 */
export interface DocumentLog {
  fileName: string;
  status: RunningStatus;
  statusName: typeof RunningStatusMap;
}

/** 文件日志表格组件 props：分页、加载态与当前 Tab。 */
export interface FileLogsTableProps {
  data: Array<IFileLogItem & DocumentLog>;
  pageCount: number;
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
  setPagination: (pagination: { page: number; pageSize: number }) => void;
  loading?: boolean;
  active: (typeof LogTabs)[keyof typeof LogTabs];
}

/** 概览顶栏各解析状态文档数量统计。 */
export interface IOverviewTotal {
  cancelled: number;
  failed: number;
  finished: number;
  processing: number;
  downloaded: number;
}

/** 单条数据流水线日志完整字段，含 DSL、进度与 pipeline 信息。 */
export interface IFileLogItem {
  create_date: string;
  create_time: number;
  document_id: string;
  document_name: string;
  document_suffix: string;
  document_type: string;
  dsl: any;
  path: string[];
  task_id: string;
  id: string;
  name: string;
  kb_id: string;
  operation_status: string;
  parser_id: string;
  pipeline_id: string;
  pipeline_title: string;
  avatar: string;
  process_begin_at: null | string;
  process_duration: number;
  progress: number;
  progress_msg: string;
  source_type?: string;
  source_from?: string;
  status: string;
  task_type: string;
  tenant_id: string;
  update_date: string;
  update_time: number;
}
/** listDataPipelineLogDocument 分页响应：logs 数组与 total。 */
export interface IFileLogList {
  logs: Array<IFileLogItem & DocumentLog>;
  total: number;
}
