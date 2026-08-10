interface Stats {
  name: string;
  value: number;
}

// HeadStats 描述 TSDB head 块内的序列数、chunk 数与时间范围。
interface HeadStats {
  numSeries: number;
  numLabelPairs: number;
  chunkCount: number;
  minTime: number;
  maxTime: number;
}

// TSDBStatusResult 汇总 head 统计与按指标名/标签名/标签值的分布计数。
// Result type for /api/v1/status/tsdb endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#tsdb-stats
export interface TSDBStatusResult {
  headStats: HeadStats;
  seriesCountByMetricName: Stats[];
  labelValueCountByLabelName: Stats[];
  memoryInBytesByLabelName: Stats[];
  seriesCountByLabelValuePair: Stats[];
}
