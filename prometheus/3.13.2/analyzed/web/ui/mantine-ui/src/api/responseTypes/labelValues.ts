// Label Values API 响应类型，对应 /api/v1/label/<name>/values。

// LabelValuesResult 为某标签名下所有 distinct 值的字符串数组。
// Result type for /api/v1/label/<label_name>/values endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#querying-label-values
export type LabelValuesResult = string[];
