// React UI 共享领域类型：指标标签、直方图、Exemplar、告警规则与 WAL 重放进度等 API 结构。

import { Alert, RuleState } from '../pages/alerts/AlertContents';

export interface Metric {
  [key: string]: string;
}

export interface Histogram {
  count: string;
  sum: string;
  buckets?: [number, string, string, string][];
}

export interface Exemplar {
  labels: { [key: string]: string };
  value: string;
  timestamp: number;
}

export interface QueryParams {
  startTime: number;
  endTime: number;
  resolution: number;
}

// Rule 聚合 alerting/recording 规则状态、查询、标签与最近评估/错误信息。
export type Rule = {
  alerts: Alert[];
  annotations: Record<string, string>;
  duration: number;
  keepFiringFor: number;
  evaluationTime: string;
  health: string;
  labels: Record<string, string>;
  lastError?: string;
  lastEvaluation: string;
  name: string;
  query: string;
  state: RuleState;
  type: string;
};

export interface WALReplayData {
  min: number;
  max: number;
  current: number;
}

// WALReplayStatus 对应 /api/v1/status/walreplay，供启动进度条渲染 min/max/current。
export interface WALReplayStatus {
  data?: WALReplayData;
}

export type ExemplarData = Array<{ seriesLabels: Metric; exemplars: Exemplar[] }> | undefined;
// types.ts 集中导出跨 alerts、graph、status 页复用的数据结构。
