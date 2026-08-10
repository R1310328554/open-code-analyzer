package constants

// VariantLabel 是多变体查询中区分序列所属变体的内部标签名。
// VariantLabel is the name of the label used to identify which variant a series belongs to
// 该标签由查询引擎注入，用户侧通常不应手动设置或持久化。
// in multi-variant queries.
const VariantLabel = "__variant__"
// VariantLabel 固定为 __variant__，与 Prometheus 内部标签命名风格一致。
