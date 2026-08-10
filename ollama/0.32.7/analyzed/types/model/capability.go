// 模型能力枚举：completion、tools、vision 等。
package model

// Capability 表示模型支持的功能类型。
type Capability string

// 预定义能力常量。
const (
	CapabilityCompletion = Capability("completion")
	CapabilityTools      = Capability("tools")
	CapabilityInsert     = Capability("insert")
	CapabilityVision     = Capability("vision")
	CapabilityEmbedding  = Capability("embedding")
	CapabilityThinking   = Capability("thinking")
	CapabilityImage      = Capability("image")
	CapabilityAudio      = Capability("audio")
)

// String 返回能力的字符串形式。
func (c Capability) String() string {
	return string(c)
}
