package summarization

// consts.go — summarization 中间件默认 token 阈值、保留消息数与摘要标记。


// DefaultMaxTokens 触发摘要前的默认最大 token 数。
const DefaultMaxTokens = 160000

// DefaultKeepMessages 摘要后默认保留的最近消息条数。
const DefaultKeepMessages = 10

// SummaryTag 标记摘要类 system 消息的前缀文本。
const SummaryTag = "[Previous conversation summarized]"
