// WALReplayStatus 报告 WAL 重放进度：min/max 段号与 current 已重放位置。
// Result type for /api/v1/status/walreplay endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#wal-replay-stats
export interface WALReplayStatus {
  min: number;
  max: number;
  current: number;
}
