package constants

// constants 集中定义 Loki v1 HTTP API 路径常量，含 LogQL 查询、元数据、索引统计、ruler 与 ingest 等端点。

// PathLoki* 常量供 router 注册与客户端构造 URL，避免硬编码字符串分散。
// Loki API v1 HTTP path constants.
const (
// query 与 query_range 为即时与范围 LogQL 查询入口路径。
	// Query and query_range
	PathLokiQueryRange = "/loki/api/v1/query_range"
	PathLokiQuery      = "/loki/api/v1/query"
	// Metadata
	PathLokiSeries          = "/loki/api/v1/series"
	PathLokiLabels          = "/loki/api/v1/labels"
	PathLokiLabel           = "/loki/api/v1/label"
	PathLokiLabelNameValues = "/loki/api/v1/label/{name}/values"
	// Index
	PathLokiIndexStats       = "/loki/api/v1/index/stats"
	PathLokiIndexShards      = "/loki/api/v1/index/shards"
	PathLokiIndexVolume      = "/loki/api/v1/index/volume"
	PathLokiIndexVolumeRange = "/loki/api/v1/index/volume_range"
	// Patterns and log metadata
	PathLokiPatterns                = "/loki/api/v1/patterns"
	PathLokiDetectedLabels          = "/loki/api/v1/detected_labels"
	PathLokiDetectedFields          = "/loki/api/v1/detected_fields"
	PathLokiDetectedFieldNameValues = "/loki/api/v1/detected_field/{name}/values"
// PathLokiTail 用于 WebSocket 实时 tail 流式日志。
	// Tail (live tailing)
	PathLokiTail = "/loki/api/v1/tail"
	// Ruler
	PathLokiRules               = "/loki/api/v1/rules"
	PathLokiRulesNamespace      = "/loki/api/v1/rules/{namespace}"
	PathLokiRulesNamespaceGroup = "/loki/api/v1/rules/{namespace}/{groupName}"
	// Delete requests (compactor)
	PathLokiDelete          = "/loki/api/v1/delete"
	PathLokiCacheGenNumbers = "/loki/api/v1/cache/generation_numbers"
	// Ingest
	PathLokiPush = "/loki/api/v1/push"
)

// PathProm* 为旧版 Prom 兼容 API，部分部署仍通过 /api/prom 访问。
// Prometheus-compatible (legacy) API path constants.
const (
	PathPromQuery           = "/api/prom/query"
	PathPromLabel           = "/api/prom/label"
	PathPromLabelPrefix     = "/api/prom/label/" // prefix for path matching
	PathPromLabelSuffix     = "/values"
	PathPromLabelNameValues = "/api/prom/label/{name}/values"
	PathPromSeries          = "/api/prom/series"
	PathPromPush            = "/api/prom/push"
	PathPromTail            = "/api/prom/tail"
	// Ruler
	PathPromRules               = "/api/prom/rules"
	PathPromRulesNamespace      = "/api/prom/rules/{namespace}"
	PathPromRulesNamespaceGroup = "/api/prom/rules/{namespace}/{groupName}"
)

// PathPrometheusRules/Alerts 供 ruler 暴露 Prometheus 风格 rules/alerts 端点。
// Prometheus API paths (used by ruler).
const (
	PathPrometheusRules  = "/prometheus/api/v1/rules"
	PathPrometheusAlerts = "/prometheus/api/v1/alerts"
)
// PathLokiPush 为 log push 写入路径，与 distributor ingester 接收端对齐。
