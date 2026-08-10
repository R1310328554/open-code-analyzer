// profile.go — 双轨配置系统：ProviderProfile 管模型构造，HarnessProfile 管运行时行为，AgentConfig 一键建 Agent。
// Package profile 提供 agentcore 双轨配置系统：
//
//   - ProviderProfile：按厂商构造 LLM（api_key、temperature 等）
//     支持 api_base、use_responses_api 等选项。
//   - HarnessProfile：按场景控制运行时（提示词、工具、中间件、迭代深度）
//     含工具描述覆盖、中间件排除与子 Agent 递归深度。
//
// Usage:
//
//	// Register once at init time.
//	profile.RegisterProvider("anthropic", &profile.ProviderProfile{
//	    InitModel: func(ctx, modelName string, opts map[string]any) (Model, error) {
//	        return anthropic.NewModel(modelName, opts["api_key"].(string)), nil
//	    },
//	    DefaultModel: "claude-sonnet-4-6",
//	})
//	profile.RegisterHarness("coding-agent", &profile.HarnessProfile{
//	    BaseSystemPrompt: strPtr("You are an expert software engineer."),
//	    MaxIterations:    20,
//	    RecursionDepth:   5,
//	})
//
//	// Create an agent in one call.
//	agent, err := profile.NewAgent(ctx, &profile.AgentConfig{
//	    ModelSpec:          "anthropic:claude-sonnet-4-6",
//	    HarnessProfileName: "coding-agent",
//	    Tools:              []core.Tool{myTool},
//	})
//
// 配置优先级（高者覆盖低者）：用户 > HarnessProfile > ProviderProfile > 默认。
package profile

import (
	"context"
	"fmt"
	"strings"
	"sync"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/internal"
	"ragflow/internal/harness/core/middlewares/subagent"
	"ragflow/internal/harness/core/schema"
)

// ========================================================================
// 阶段 1：核心类型与全局注册表
// ========================================================================

// ProviderProfile 定义某厂商如何构造 Model。
// 各厂商注册一个 ProviderProfile。
// InitModel 根据模型名与 opts 创建具体 Model。
type ProviderProfile struct {
	// Name 厂商标识，如 anthropic、openai。
	Name string

	// InitModel 创建 Model 实例。
	// opts 通常含 api_key、temperature、max_tokens、api_base 等。
	InitModel func(ctx context.Context, modelName string, opts map[string]any) (core.Model[*schema.Message], error)

	// DefaultModel 未指定模型名时的默认值。
	DefaultModel string

	// DefaultOpts InitModel 默认选项，可被 Harness 或用户覆盖。
	// HarnessProfile 或 AgentConfig.ProviderOpts 可覆盖。
	DefaultOpts map[string]any
}

// HarnessProfile 控制特定场景下的 Agent 运行时行为。
// 同一模型可搭配不同 Harness 配置。
type HarnessProfile struct {
	// Name 配置名，如 coding-agent。
	Name string

	// BaseSystemPrompt 完全替换默认系统提示；nil 则用系统默认。
	// nil 时使用 internal.DefaultSystemPrompt。
	BaseSystemPrompt *string

	// SystemPromptSuffix 追加在 BaseSystemPrompt 之后。
	SystemPromptSuffix string

	// ToolDescriptionOverrides 按工具名覆盖 Description。
	// 键为工具名，值为新描述。
	ToolDescriptionOverrides map[string]string

	// ExcludedToolNames 从工具列表移除指定工具。
	ExcludedToolNames []string

	// ExcludedMiddlewareNames 按类型名排除中间件。
	// 匹配 fmt.Sprintf("%T", mw)，需写完整类型名。
	// 例如 "*subagent.SubAgentMiddleware"。
	ExcludedMiddlewareNames []string

	// ExtraMiddlewares 追加到中间件链末尾。
	ExtraMiddlewares []core.ReActMiddleware

	// MaxIterations 覆盖 ReAct 最大迭代次数；0 表示默认。
	MaxIterations int

	// RecursionDepth 子 Agent 递归深度上限。
	// 0 表示使用系统默认（无限制）。
	RecursionDepth int
}

// 全局注册表（sync.Map）。
var (
	providers sync.Map // map[string]*ProviderProfile
	harnesses sync.Map // map[string]*HarnessProfile
)

// RegisterProvider 注册 Provider；重名 panic。
func RegisterProvider(p *ProviderProfile) {
	if p == nil {
		panic("profile: RegisterProvider called with nil")
	}
	if p.Name == "" {
		panic("profile: ProviderProfile.Name is required")
	}
	if _, loaded := providers.LoadOrStore(p.Name, p); loaded {
		panic(fmt.Sprintf("profile: provider %q already registered", p.Name))
	}
}

// RegisterHarness 注册 Harness；重名 panic。
func RegisterHarness(h *HarnessProfile) {
	if h == nil {
		panic("profile: RegisterHarness called with nil")
	}
	if h.Name == "" {
		panic("profile: HarnessProfile.Name is required")
	}
	if _, loaded := harnesses.LoadOrStore(h.Name, h); loaded {
		panic(fmt.Sprintf("profile: harness profile %q already registered", h.Name))
	}
}

// LookupProvider 查找 Provider，未找到返回 nil。
func LookupProvider(name string) *ProviderProfile {
	if v, ok := providers.Load(name); ok {
		return v.(*ProviderProfile)
	}
	return nil
}

// LookupHarness 查找 Harness，未找到返回 nil。
func LookupHarness(name string) *HarnessProfile {
	if v, ok := harnesses.Load(name); ok {
		return v.(*HarnessProfile)
	}
	return nil
}

// ParseModelSpec 解析 "provider:model" 规格字符串。
// 例如 "anthropic:claude-sonnet-4-6" → (anthropic, claude-sonnet-4-6)。
// 无冒号则返回格式错误。
func ParseModelSpec(spec string) (provider, model string, err error) {
	parts := strings.SplitN(spec, ":", 2)
	if len(parts) != 2 || parts[0] == "" || parts[1] == "" {
		return "", "", fmt.Errorf("profile: invalid model spec %q (expected provider:model)", spec)
	}
	return parts[0], parts[1], nil
}

// ========================================================================
// AgentConfig 声明式组合模型规格、Harness 与用户覆盖项。
// ========================================================================

// AgentConfig 高层声明式配置，用于一键创建 ReActAgent。
// 通过 ModelSpec 选模型，HarnessProfileName 选运行时配置。
// 用户字段优先级最高。
type AgentConfig struct {
	// ModelSpec 格式 provider:model。
	ModelSpec string

	// HarnessProfileName 选择已注册 Harness；空串表示不用。
	// 空串时不应用 Harness 层覆盖。
	HarnessProfileName string

	// ProviderOpts 覆盖或补充 Provider DefaultOpts。
	ProviderOpts map[string]any

	// Instruction 非 nil 时覆盖系统提示（最高优先级）。
	// 优先于 Harness.BaseSystemPrompt 与系统默认。
	Instruction *string

	// Tools 非 nil 时替换全部工具列表。
	Tools []core.Tool

	// Middlewares 追加在 Harness.ExtraMiddlewares 之后。
	Middlewares []core.ReActMiddleware

	// MaxIterations 覆盖 Harness 与系统默认。
	MaxIterations int

	// SubAgentSpecs 声明子 Agent，自动创建 SubAgentMiddleware。
	// 递归深度取自当前 HarnessProfile.RecursionDepth。
	SubAgentSpecs []subagent.SubAgentSpec
}

// ========================================================================
// 阶段 3：带优先级的覆盖链
// ========================================================================

// NewAgent 从 AgentConfig 创建 ReActAgent。
//
// 优先级：用户显式配置 > Harness > Provider > 默认。
func NewAgent(ctx context.Context, cfg *AgentConfig) (core.Agent, error) {
	if cfg == nil {
		return nil, fmt.Errorf("profile: AgentConfig is nil")
	}

	// 1. 从 ModelSpec 解析并构造 Model。
	model, err := buildModel(ctx, cfg)
	if err != nil {
		return nil, err
	}

	// 2. 经覆盖链构建 ReActConfig。
	reactCfg := buildReactConfig(ctx, cfg)

	// 3. 写入解析后的 Model。
	reactCfg.Model = model

	// 4. 应用 ExcludedToolNames 过滤工具。
	if harness := lookupHarness(cfg.HarnessProfileName); harness != nil && len(harness.ExcludedToolNames) > 0 {
		excluded := makeMap(harness.ExcludedToolNames)
		filtered := make([]core.Tool, 0, len(reactCfg.Tools))
		for _, t := range reactCfg.Tools {
			if excluded[t.Name()] {
				continue
			}
			filtered = append(filtered, t)
		}
		reactCfg.Tools = filtered
	}

	// 5. 用 descriptionOverrideTool 包装描述覆盖。
	if harness := lookupHarness(cfg.HarnessProfileName); harness != nil && len(harness.ToolDescriptionOverrides) > 0 {
		for i, t := range reactCfg.Tools {
			if newDesc, ok := harness.ToolDescriptionOverrides[t.Name()]; ok && newDesc != "" {
				reactCfg.Tools[i] = &descriptionOverrideTool{Tool: t, newDesc: newDesc}
			}
		}
	}

	// 6. 处理 SubAgentSpecs，创建并 Init SubAgentMiddleware。
	if len(cfg.SubAgentSpecs) > 0 {
		subCfg := &subagent.Config{}
		if harness := lookupHarness(cfg.HarnessProfileName); harness != nil && harness.RecursionDepth > 0 {
			subCfg.MaxDepth = harness.RecursionDepth
		}
		saMW := subagent.New(cfg.SubAgentSpecs, subCfg)
		reactCfg.Middlewares = append(reactCfg.Middlewares, saMW)
		saMW.Init(ctx, reactCfg)
	}

	return core.NewReActAgent(reactCfg), nil
}

// buildModel 从 ModelSpec 与 ProviderProfile 构造 Model。
func buildModel(ctx context.Context, cfg *AgentConfig) (core.Model[*schema.Message], error) {
	providerName, modelName, err := ParseModelSpec(cfg.ModelSpec)
	if err != nil {
		return nil, err
	}

	provider := LookupProvider(providerName)
	if provider == nil {
		return nil, fmt.Errorf("profile: unknown provider %q (registered: %s)", providerName, listProviders())
	}

	// 合并选项：DefaultOpts 被 ProviderOpts 覆盖。
	opts := copyMap(provider.DefaultOpts)
	for k, v := range cfg.ProviderOpts {
		opts[k] = v
	}

	m, err := provider.InitModel(ctx, modelName, opts)
	if err != nil {
		return nil, fmt.Errorf("profile: InitModel(%s, %s): %w", providerName, modelName, err)
	}
	return m, nil
}

// buildReactConfig 对非模型字段应用覆盖链。
func buildReactConfig(ctx context.Context, cfg *AgentConfig) *core.ReActConfig[*schema.Message] {
	// 从系统默认值起步。
	result := &core.ReActConfig[*schema.Message]{
		MaxIterations: 10,
		Instruction:   internal.DefaultSystemPrompt,
	}

	harness := lookupHarness(cfg.HarnessProfileName)

	// 第 1 层：HarnessProfile。
	if harness != nil {
		if harness.MaxIterations > 0 {
			result.MaxIterations = harness.MaxIterations
		}
		if harness.BaseSystemPrompt != nil {
			result.Instruction = *harness.BaseSystemPrompt
		}
		result.Instruction += harness.SystemPromptSuffix

		// 追加 ExtraMiddlewares；ExcludedMiddlewareNames 稍后过滤。
		result.Middlewares = append(result.Middlewares, harness.ExtraMiddlewares...)
	}

	// 第 2 层：用户显式配置（最高优先级）。
	if cfg.Instruction != nil {
		result.Instruction = *cfg.Instruction
	}
	if cfg.Tools != nil {
		result.Tools = cfg.Tools
	}
	if cfg.MaxIterations > 0 {
		result.MaxIterations = cfg.MaxIterations
	}
	if cfg.Middlewares != nil {
		result.Middlewares = append(result.Middlewares, cfg.Middlewares...)
	}

	// 应用 Harness 的中间件排除列表。
	if harness != nil && len(harness.ExcludedMiddlewareNames) > 0 {
		result.Middlewares = filterMiddlewareByTypeName(result.Middlewares, harness.ExcludedMiddlewareNames)
	}

	return result
}

// ========================================================================
// 辅助函数
// ========================================================================

func lookupHarness(name string) *HarnessProfile {
	if name == "" {
		return nil
	}
	return LookupHarness(name)
}

func listProviders() string {
	var names []string
	providers.Range(func(key, _ any) bool {
		names = append(names, key.(string))
		return true
	})
	return strings.Join(names, ", ")
}

func copyMap(src map[string]any) map[string]any {
	if src == nil {
		return make(map[string]any)
	}
	dst := make(map[string]any, len(src))
	for k, v := range src {
		dst[k] = v
	}
	return dst
}

func makeMap(keys []string) map[string]bool {
	m := make(map[string]bool, len(keys))
	for _, k := range keys {
		m[k] = true
	}
	return m
}

// filterMiddlewareByTypeName 按类型名排除中间件。
// exclude 列表中的类型名将被移除。
func filterMiddlewareByTypeName(mws []core.ReActMiddleware, exclude []string) []core.ReActMiddleware {
	if len(exclude) == 0 {
		return mws
	}
	excluded := makeMap(exclude)
	filtered := make([]core.ReActMiddleware, 0, len(mws))
	for _, mw := range mws {
		if mw == nil {
			continue
		}
		typeName := fmt.Sprintf("%T", mw)
		if excluded[typeName] {
			continue
		}
		filtered = append(filtered, mw)
	}
	return filtered
}

// descriptionOverrideTool 包装 Tool 以覆盖 Description。
type descriptionOverrideTool struct {
	core.Tool
	newDesc string
}

func (t *descriptionOverrideTool) Description() string { return t.newDesc }

// StrPtr 创建 *string 字面量辅助函数。
func StrPtr(s string) *string { return &s }

// Validate 批量校验 AgentConfig 常见错误。
func Validate(cfg *AgentConfig) []error {
	var errs []error
	if cfg == nil {
		return []error{fmt.Errorf("profile: AgentConfig is nil")}
	}
	if cfg.ModelSpec == "" {
		errs = append(errs, fmt.Errorf("profile: ModelSpec is required"))
	} else if _, _, err := ParseModelSpec(cfg.ModelSpec); err != nil {
		errs = append(errs, err)
	}
	if cfg.HarnessProfileName != "" && LookupHarness(cfg.HarnessProfileName) == nil {
		errs = append(errs, fmt.Errorf("profile: harness profile %q not found", cfg.HarnessProfileName))
	}
	return errs
}

// ClearProviders 清空 Provider 注册表（测试隔离）。
func ClearProviders() {
	providers = sync.Map{}
}

// ClearHarnesses 清空 Harness 注册表（测试隔离）。
func ClearHarnesses() {
	harnesses = sync.Map{}
}

// RegisterProviderModel 同时注册 Provider 与多个 Harness（便捷包装）。
// 匹配 deepagents 双重注册模式。
// 已废弃：建议分别调用 RegisterProvider / RegisterHarness。
//
// Deprecated：请改用独立 Register 调用。
func RegisterProviderModel(provider *ProviderProfile, harnessProfiles ...*HarnessProfile) {
	RegisterProvider(provider)
	for _, h := range harnessProfiles {
		RegisterHarness(h)
	}
}

// NewAgent 是 profile 包主入口：解析 ModelSpec、合并三层配置并返回 ReActAgent。
