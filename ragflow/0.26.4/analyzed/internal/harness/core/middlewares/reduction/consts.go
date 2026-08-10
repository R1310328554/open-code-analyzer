package reduction

// consts.go — reduction 中间件默认阈值：工具输出截断长度与保留调用数。


// DefaultMaxToolOutputLen 工具输出默认最大字符数，超出则截断。
const DefaultMaxToolOutputLen = 2000

// DefaultMaxToolCalls 默认保留的 tool 调用条数上限。
const DefaultMaxToolCalls = 20

// 零值或未配置时 NewTyped 会回退到上述默认值。
