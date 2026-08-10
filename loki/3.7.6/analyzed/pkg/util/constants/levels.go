package constants

// constants 包定义日志级别标签名与标准级别字符串常量，供结构化元数据、查询过滤与指标命名统一使用。

const (
	LevelLabel       = "detected_level"
	LogLevelUnknown  = "unknown"
	LogLevelDebug    = "debug"
	LogLevelInfo     = "info"
	LogLevelWarn     = "warn"
	LogLevelError    = "error"
	LogLevelFatal    = "fatal"
	LogLevelCritical = "critical"
	LogLevelTrace    = "trace"
)

// LogLevels 按优先级顺序列出全部级别，便于校验与 UI 下拉选项生成。
var LogLevels = []string{
	LogLevelUnknown,
	LogLevelDebug,
	LogLevelInfo,
	LogLevelWarn,
	LogLevelError,
	LogLevelFatal,
	LogLevelCritical,
	LogLevelTrace,
}
// 未知级别 LogLevelUnknown 作为缺省占位，避免空标签导致查询匹配失败。
