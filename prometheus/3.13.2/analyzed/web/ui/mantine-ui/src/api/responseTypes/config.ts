// Status Config API 响应类型，对应 /api/v1/status/config 端点。

// ConfigResult 封装当前生效的 Prometheus 配置 YAML 字符串。
// Result type for /api/v1/status/config endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#config
export default interface ConfigResult {
  yaml: string;
}
