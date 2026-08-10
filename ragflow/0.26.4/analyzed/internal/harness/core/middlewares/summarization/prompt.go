// prompt.go — summarization 内部事件 action 类型常量。

// Package summarization 提供摘要相关 prompt 与事件类型常量。
package summarization

// EmitInternalEvents 使用的摘要生命周期 action 类型
const (
	ActionTypeBeforeSummarize = "summarize:before"
	ActionTypeAfterSummarize  = "summarize:after"
	ActionTypeGenerateSummary = "summarize:generate"
)

// before/after/generate 三阶段便于观测摘要触发与 LLM 生成过程。
