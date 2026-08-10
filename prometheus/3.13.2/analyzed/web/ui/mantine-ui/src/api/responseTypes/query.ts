// Query API 响应类型：instant/range 查询的 vector、matrix、scalar 与 histogram 样本结构。

export interface Metric {
  [key: string]: string;
}

export interface Histogram {
  count: string;
  sum: string;
  buckets?: [number, string, string, string][];
}

export interface InstantSample {
  metric: Metric;
  value?: SampleValue;
  histogram?: SampleHistogram;
}

export interface RangeSamples {
  metric: Metric;
  values?: SampleValue[];
  histograms?: SampleHistogram[];
}

export type SampleValue = [number, string];
export type SampleHistogram = [number, Histogram];

// QueryStats 对应 API 返回的 timings 与 samples 查询统计。
export type QueryStats = {
  timings: Record<string, number>;
  samples: Record<string, number>;
};

// InstantQueryResult 为 /query 的 data 字段，按 resultType 区分四种结果形态。
// Result type for /api/v1/query endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#instant-queries
export type InstantQueryResult = (
  | {
      resultType: "vector";
      result: InstantSample[];
    }
  | {
      resultType: "matrix";
      result: RangeSamples[];
    }
  | {
      resultType: "scalar";
      result: SampleValue;
    }
  | {
      resultType: "string";
      result: SampleValue;
    }
) & { stats?: QueryStats };

// RangeQueryResult 固定 resultType=matrix，result 为区间样本序列。
// Result type for /api/v1/query_range endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#range-queries
export type RangeQueryResult = {
  resultType: "matrix";
  result: RangeSamples[];
  stats?: QueryStats;
};
