/**
 * data-source/interface.ts — 数据源连接器类型与同步任务日志字段。
 */

import { RunningStatus } from '@/constants/knowledge';
import { DataSourceKey } from './constant';

/** 数据源类型卡片（名称、描述、图标）；拼写保留 IDataSorceInfo。 */
export interface IDataSorceInfo {
  id: DataSourceKey;
  name: string;
  description: string;
  icon: React.ReactNode;
}

/** 数据源详情：调度频率、索引状态与租户信息。 */
export type IDataSource = IDataSourceBase & {
  config: any;
  indexing_start: null | string;
  input_type: string;
  prune_freq: number;
  refresh_freq: number;
  status: string;
  tenant_id: string;
  update_date: string;
  update_time: number;
};

/** 列表项：id、名称与 source 枚举键。 */
export interface IDataSourceBase {
  id: string;
  name: string;
  source: DataSourceKey;
}

/** 单次同步/修剪任务日志与文档计数。 */
export interface IDataSourceLog {
  connector_id: string;
  docs_removed_from_index?: number;
  error_count: number;
  error_msg: string;
  id: string;
  kb_id: string;
  kb_name: string;
  new_docs_indexed: number;
  prune_freq?: number;
  refresh_freq?: number;
  status: RunningStatus;
  task_type?: string;
  time_started?: string | null;
  total_docs_indexed?: number;
  update_date: string;
}

interface IDataSourceInfoItem {
  name: string;
  description: string;
  icon: JSX.Element;
}

/** 各 DataSourceKey 对应的 UI 展示配置。 */
export type IDataSourceInfoMap = Record<DataSourceKey, IDataSourceInfoItem>;
