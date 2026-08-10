// Metadata API 响应类型，对应 /api/v1/metadata 指标元数据端点。

// MetadataResult 映射指标名到 type/help/unit 元数据条目列表（注释链接指向 metadata 文档）。
// Result type for /api/v1/alerts endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#querying-target-metadata
export type MetadataResult = Record<
  string,
  { type: string; help: string; unit: string }[]
>;
