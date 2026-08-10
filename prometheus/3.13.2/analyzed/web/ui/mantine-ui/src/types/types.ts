// Prometheus 直方图样本通用类型：count、sum 与可选 buckets 元组数组。

export interface Histogram {
  count: string;
  sum: string;
  buckets?: [number, string, string, string][];
}
