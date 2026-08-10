package log

// error 定义日志流水线各 Stage 写入 __error__ 标签时使用的标准错误标识字符串常量。

var (
// 以下常量对应 JSON/Logfmt 解析、采样提取、标签过滤与模板格式化等阶段的失败类型。
	// Possible errors thrown by a log pipeline.
	errJSON             = "JSONParserErr"
	errLogfmt           = "LogfmtParserErr"
	errSampleExtraction = "SampleExtractionErr"
	errLabelFilter      = "LabelFilterErr"
	errTemplateFormat   = "TemplateFormatErr"
)
// errLabelFilter 与 errTemplateFormat 分别对应标签谓词求值与 line_format/label_format 模板执行错误。
