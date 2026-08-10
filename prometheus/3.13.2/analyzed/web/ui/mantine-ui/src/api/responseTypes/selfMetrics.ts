// SelfMetricsResult 为 Prometheus 进程自身 exposition 的 ProtoJSON MetricFamily 数组。
// Result type for /api/v1/status/self_metrics endpoint.
// 字段命名与 protobuf JSON 映射一致，便于直接反序列化 client_model。
// The response uses the standard ProtoJSON format for io.prometheus.client.MetricFamily.
// See https://protobuf.dev/programming-guides/json/

export interface ProtoLabelPair {
  name: string;
  value: string;
}

export interface ProtoGauge {
  value: number;
}

export interface ProtoCounter {
  value: number;
  exemplar?: ProtoExemplar;
  createdTimestamp?: string;
}

export interface ProtoQuantile {
  quantile: number;
  value: number;
}

export interface ProtoSummary {
  sampleCount: string;
  sampleSum: number;
  quantile?: ProtoQuantile[];
  createdTimestamp?: string;
}

export interface ProtoBucket {
  cumulativeCount: string;
  cumulativeCountFloat?: number;
  upperBound: number;
  exemplar?: ProtoExemplar;
}

export interface ProtoBucketSpan {
  offset: number;
  length: number;
}

// ProtoHistogram 支持经典桶与原生指数直方图的 span/delta 字段。
export interface ProtoHistogram {
  sampleCount: string;
  sampleCountFloat?: number;
  sampleSum: number;
  bucket?: ProtoBucket[];
  createdTimestamp?: string;
  schema?: number;
  zeroThreshold?: number;
  zeroCount?: string;
  zeroCountFloat?: number;
  negativeSpan?: ProtoBucketSpan[];
  negativeDelta?: string[];
  negativeCount?: number[];
  positiveSpan?: ProtoBucketSpan[];
  positiveDelta?: string[];
  positiveCount?: number[];
  exemplars?: ProtoExemplar[];
}

export interface ProtoExemplar {
  label?: ProtoLabelPair[];
  value: number;
  timestamp?: string;
}

export interface ProtoUntyped {
  value: number;
}

export interface ProtoMetric {
  label?: ProtoLabelPair[];
  gauge?: ProtoGauge;
  counter?: ProtoCounter;
  summary?: ProtoSummary;
  histogram?: ProtoHistogram;
  untyped?: ProtoUntyped;
  timestampMs?: string;
}

// ProtoMetricFamily 对应 MetricFamily：name、help、type 与 metric 样本列表。
export interface ProtoMetricFamily {
  name: string;
  help: string;
  type: string;
  metric: ProtoMetric[];
  unit?: string;
}

export type SelfMetricsResult = ProtoMetricFamily[];
