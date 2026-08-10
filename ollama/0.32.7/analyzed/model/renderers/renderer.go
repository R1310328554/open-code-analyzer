// 模型渲染器注册表：按名称构造 Renderer 并渲染聊天模板。
package renderers

import (
	"fmt"

	"github.com/ollama/ollama/api"
)

// Renderer 将消息序列化为模型特定的 prompt 字符串。
type Renderer interface {
	Render(messages []api.Message, tools []api.Tool, think *api.ThinkValue) (string, error)
	LeadingBOS() string
}

// RendererConstructor 为 Renderer 工厂函数。
// RendererRegistry 维护名称到构造器的映射。
type (
	RendererConstructor func() Renderer
	RendererRegistry    struct {
		renderers map[string]RendererConstructor
	}
)

// RenderImgTags 全局开关：为 true 时渲染器用 [img] 占位符表示图像。
// RenderImgTags is a global flag that tells renderers to use [img] tags
// for images. This is set by the Ollama server package on init, or left as
// false for other environments where renderers are used
var RenderImgTags bool

// Register 向注册表添加命名渲染器构造器。
func (r *RendererRegistry) Register(name string, renderer RendererConstructor) {
	r.renderers[name] = renderer
}

var registry = RendererRegistry{
	renderers: make(map[string]RendererConstructor),
}

// Register 在全局 registry 上注册渲染器。
func Register(name string, renderer RendererConstructor) {
	registry.Register(name, renderer)
}

// RenderWithRenderer 按名称查找渲染器并渲染消息。
func RenderWithRenderer(name string, msgs []api.Message, tools []api.Tool, think *api.ThinkValue) (string, error) {
	renderer := rendererForName(name)
	if renderer == nil {
		return "", fmt.Errorf("unknown renderer %q", name)
	}
	return renderer.Render(msgs, tools, think)
}

// LeadingBOSForRenderer 返回指定渲染器的 BOS 前缀 token。
func LeadingBOSForRenderer(name string) string {
	renderer := rendererForName(name)
	if renderer == nil {
		return ""
	}

	return renderer.LeadingBOS()
}

// rendererForName 按内置名称或注册表解析 Renderer 实例。
func rendererForName(name string) Renderer {
	if constructor, ok := registry.renderers[name]; ok {
		return constructor()
	}
	switch name {
	case "qwen3-coder":
		renderer := &Qwen3CoderRenderer{}
		return renderer
	case "qwen3-vl-instruct":
		renderer := &Qwen3VLRenderer{isThinking: false, useImgTags: RenderImgTags}
		return renderer
	case "qwen3-vl-thinking":
		renderer := &Qwen3VLRenderer{isThinking: true, useImgTags: RenderImgTags}
		return renderer
	case "qwen3.5":
		renderer := &Qwen35Renderer{isThinking: true, emitEmptyThinkOnNoThink: true, useImgTags: RenderImgTags}
		return renderer
	case "ornith":
		return newOrnithRenderer()
	case "cogito":
		renderer := &CogitoRenderer{isThinking: true}
		return renderer
	case "deepseek3.1":
		renderer := &DeepSeek3Renderer{IsThinking: true, Variant: Deepseek31}
		return renderer
	case "olmo3":
		renderer := &Olmo3Renderer{UseExtendedSystemMessage: false}
		return renderer
	case "olmo3.1":
		renderer := &Olmo3Renderer{UseExtendedSystemMessage: true}
		return renderer
	case "olmo3-think":
		// 用于 Olmo-3-7B-Think 与 Olmo-3.1-32B-Think（同一模板）
		// Used for Olmo-3-7B-Think and Olmo-3.1-32B-Think (same template)
		renderer := &Olmo3ThinkRenderer{Variant: Olmo31Think}
		return renderer
	case "olmo3-32b-think":
		// 用于 Olmo-3-32B-Think
		// Used for Olmo-3-32B-Think
		renderer := &Olmo3ThinkRenderer{Variant: Olmo3Think32B}
		return renderer
	case "nemotron-3-nano":
		return &Nemotron3NanoRenderer{}
	case "gemma4", "gemma4-small":
		return &Gemma4Renderer{useImgTags: RenderImgTags}
	case "gemma4-large":
		return &Gemma4Renderer{useImgTags: RenderImgTags, emptyBlockOnNothink: true}
	case "functiongemma":
		return &FunctionGemmaRenderer{}
	case "glm-4.7":
		return &GLM47Renderer{}
	case "glm-ocr":
		return &GlmOcrRenderer{useImgTags: RenderImgTags}
	case "lfm2":
		return &LFM2Renderer{IsThinking: false, useImgTags: RenderImgTags}
	case "lfm2-thinking":
		return &LFM2Renderer{IsThinking: true, useImgTags: RenderImgTags}
	case "laguna":
		return &LagunaRenderer{}
	case "poolside-v1":
		return &LagunaV8Renderer{}
	case "cohere":
		return &CohereRenderer{}
	case "glimmer":
		return &GlimmerRenderer{useImgTags: RenderImgTags}
	default:
		return nil
	}
}
