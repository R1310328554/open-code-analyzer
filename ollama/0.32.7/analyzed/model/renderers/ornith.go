// Ornith 渲染器：基于 Qwen3.5 的 thinking 多模态变体。
package renderers

// OrnithRenderer 嵌入 Qwen35Renderer 并固定 thinking/图像配置。
type OrnithRenderer struct {
	Qwen35Renderer
}

// newOrnithRenderer 构造启用 thinking 与 [img] 的 Ornith 渲染器。
func newOrnithRenderer() Renderer {
	return &OrnithRenderer{
		Qwen35Renderer: Qwen35Renderer{
			isThinking:                      true,
			alwaysRenderAssistantThinkBlock: true,
			emitEmptyThinkOnNoThink:         true,
			useImgTags:                      RenderImgTags,
		},
	}
}
