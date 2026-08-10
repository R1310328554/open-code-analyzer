package constants

// constants 包集中声明 Prometheus 指标命名空间前缀：区分 Loki、Cortex 与 OTLP 导出路径下的 metric 前缀。

const (
	Loki   = "loki"
	Cortex = "cortex"
	OTLP   = "otlp"
)
// 保持命名空间常量集中定义，避免各子包硬编码导致指标前缀不一致。
