// subagent.go — 子 Agent 中间件：将子 Agent 注入父 ReAct 的工具列表，支持声明式配置、中间件继承与递归深度限制。

// Package subagent 将子 Agent 作为动态工具注入父 ReAct Agent。
// 支持声明式 AgentConfig、中间件继承与 MaxDepth 递归保护。
// inheritance, and recursion depth protection.
//
// Quick Start:
//
//	// Declarative sub-agent config (no pre-built Agent needed).
//	spec := subagent.SubAgentSpec{
//	    Name:        "researcher",
//	    Description: "Research a topic using web search",
//	    AgentConfig: &subagent.AgentConfig{
//	        Model:        anthropicModel,
//	        Tools:        []core.Tool{searchTool},
//	        SystemPrompt: "You are a research assistant.",
//	    },
//	}
//	mw := subagent.New([]subagent.SubAgentSpec{spec}, &subagent.Config{
//	    EmitInternalEvents: true,
//	    MaxDepth:           5,
//	})
//
//	cfg := &core.ReActConfig[*schema.Message]{
//	    Model:       parentModel,
//	    Middlewares: []core.ReActMiddleware{mw, filesystemMW},
//	}
//	mw.BindToConfig(ctx, cfg)  // injects sub-agent tools + forces inline dispatch
//	agent := core.NewReActAgent(cfg)
//
// InheritParentMiddlewares 为 true 时自动继承父 Agent 非 subagent 中间件。
// (e.g. filesystem) when InheritParentMiddlewares is true on the spec.
//
// MaxDepth 限制嵌套深度；超限时通过 context 传播返回错误。
// returns an error (checked via context.Context value propagation across
// AgentTool invocations).
package subagent

import (
	"context"
	"fmt"
	"sync"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// ---- 中间件继承过滤标记接口 ----

type subAgentMarker interface{ isSubAgentMiddleware() }

// ---- 配置类型 ----

// SubAgentSpec 声明可由父 Agent LLM 通过工具调用的子 Agent。
// LLM via a tool call.
//
// Agent 与 AgentConfig 至少设置其一；两者皆空则跳过该 spec。
//   - Agent: a pre-built core.Agent instance.
//   - AgentConfig: declarative config from which the Agent is built on BindToConfig.
//
// When both are nil, the spec is silently skipped.
type SubAgentSpec struct {
	// Name 为 LLM 调用子 Agent 的工具名。
	Name string
	// Description 展示给 LLM 的工具描述。
	Description string

	// Agent is a pre-built Agent instance. Mutually exclusive with AgentConfig
	// (AgentConfig takes precedence when both are set).
	Agent core.Agent

	// AgentConfig 声明式描述子 Agent，BindToConfig/Init 时构建。
	// from this config when BindToConfig is called. Overrides Agent when both set.
	AgentConfig *AgentConfig

	// AgentFactory is called on first use (inside BindToConfig) to create the
	// Agent. Ignored when either Agent or AgentConfig is set.
	AgentFactory func(ctx context.Context) (core.Agent, error)

	// InheritParentMiddlewares copies the parent agent's non-subagent middlewares
	// into this sub-agent's middleware chain. The SubAgentMiddleware itself is
	// automatically excluded to prevent infinite recursion. Additional middlewares
	// can be excluded via ExcludedParentMiddlewareNames.
	//
	// Inherited middlewares are prepended before AgentConfig.Middlewares.
	InheritParentMiddlewares bool

	// ExcludedParentMiddlewareNames lists the fully-qualified type names (as
	// returned by fmt.Sprintf("%T", mw)) of parent middlewares to skip when
	// InheritParentMiddlewares is true. For example:
	//   "*filesystem.middleware[*schema.Message]"
	ExcludedParentMiddlewareNames []string
}

// AgentConfig declaratively describes an agent to be built by the
// SubAgentMiddleware. Use this instead of providing a pre-built Agent.
type AgentConfig struct {
	// Model is the chat model for the sub-agent.
	Model core.Model[*schema.Message]

	// Tools available to the sub-agent.
	Tools []core.Tool

	// SystemPrompt is the system instruction for the sub-agent.
	SystemPrompt string

	// MaxIterations limits the ReAct loop (default: 10).
	MaxIterations int

	// Middlewares specific to this sub-agent. When InheritParentMiddlewares
	// is true, these are appended AFTER inherited parent middlewares.
	Middlewares []core.ReActMiddleware
}

// Config 配置 SubAgentMiddleware 行为。
type Config struct {
	// EmitInternalEvents 将子 Agent 内部事件转发到父事件流。
	// parent agent's event stream.
	EmitInternalEvents bool

	// MaxDepth 限制子 Agent 递归深度，0 表示不限。
	// A depth of 1 allows one level of sub-agent nesting (parent → sub).
	// Each nested AgentTool call increments the depth via context.Context.
	MaxDepth int
}

// ---- 中间件实现 ----

// SubAgentMiddleware 将子 Agent 包装为 AgentTool 注入父 Agent。
//
// 工具贡献经 ToolContributor；需继承时调用 Init 传入父 ReActConfig。
// This eliminates the need to call BindToConfig in most cases. For specs that need
// middleware inheritance (InheritParentMiddlewares), call Init(ctx, parentConfig)
// after adding the middleware to the config's Middlewares slice.
//
// BindToConfig 已弃用，优先 Init 或 ToolContributor 自动收集。
// Init or the ToolContributor interface directly.
type SubAgentMiddleware struct {
	core.BaseMiddleware[*schema.Message]

	cfg        *Config
	specs      []SubAgentSpec
	mu         sync.Mutex
	tools      []core.Tool // AgentTool wrappers, built in ensureBuilt
	infos      []*schema.ToolInfo
	builtInfos []*schema.ToolInfo // only specs that were actually built
	built      bool
	parentCfg  *core.ReActConfig[*schema.Message] // stored by Init for middleware inheritance
}

// New 创建中间件；cfg 为 nil 时使用默认配置。
//
// specs are validated immediately; AgentTool wrappers are created lazily in
// BindToConfig (where the parent's ReActConfig is available for middleware
// inheritance).
func New(specs []SubAgentSpec, cfg *Config) *SubAgentMiddleware {
	if cfg == nil {
		cfg = &Config{}
	}
	// Pre-build ToolInfo entries (names/descriptions are always available).
	infos := make([]*schema.ToolInfo, 0, len(specs))
	for _, spec := range specs {
		infos = append(infos, &schema.ToolInfo{
			Name:        spec.Name,
			Description: spec.Description,
		})
	}
	return &SubAgentMiddleware{
		cfg:   cfg,
		specs: specs,
		infos: infos,
	}
}

// ---- ToolContributor 接口 ----
//
// ContributeTools 返回可构建 spec 的 AgentTool 包装器。
// without middleware inheritance. For specs needing InheritParentMiddlewares,
// call Init(ctx, parentConfig) before the agent is built.
func (m *SubAgentMiddleware) ContributeTools(ctx context.Context) []core.Tool {
	// If Init was called with a parent config, ensureBuilt handles inheritance.
	if m.parentCfg != nil {
		m.ensureBuilt(ctx, m.parentCfg)
	} else {
		// No parent config: build simple agents (no middleware inheritance).
		m.ensureBuiltSimple(ctx)
	}
	m.mu.Lock()
	tools := make([]core.Tool, len(m.tools))
	copy(tools, m.tools)
	m.mu.Unlock()
	return tools
}

func (m *SubAgentMiddleware) ContributeToolInfos(ctx context.Context) []*schema.ToolInfo {
	if m.parentCfg != nil {
		m.ensureBuilt(ctx, m.parentCfg)
	} else {
		m.ensureBuiltSimple(ctx)
	}
	m.mu.Lock()
	infos := make([]*schema.ToolInfo, len(m.builtInfos))
	copy(infos, m.builtInfos)
	m.mu.Unlock()
	return infos
}

func (m *SubAgentMiddleware) ContributeReturnDirectly(ctx context.Context) map[string]bool {
	// Sub-agent tools should not cause the parent to return directly.
	return nil
}

// ensureBuiltSimple 无父配置时构建全部 spec（无中间件继承）。
func (m *SubAgentMiddleware) ensureBuiltSimple(ctx context.Context) {
	m.mu.Lock()
	if m.built {
		m.mu.Unlock()
		return
	}
	m.built = true
	m.mu.Unlock()

	for _, spec := range m.specs {
		agent := m.resolveAgent(ctx, spec, nil)
		if agent == nil {
			continue
		}
		m.mu.Lock()
		m.builtInfos = append(m.builtInfos, &schema.ToolInfo{
			Name: spec.Name, Description: spec.Description,
		})
		opts := []core.AgentToolOption{}
		if m.cfg.EmitInternalEvents {
			opts = append(opts, core.WithEmitInternalEvents())
		}
		if m.cfg.MaxDepth > 0 {
			opts = append(opts, core.WithMaxDepth(m.cfg.MaxDepth))
		}
		tool := core.NewAgentTool(ctx, agent, opts...)
		m.tools = append(m.tools, tool)
		m.mu.Unlock()
	}
}

// Init 绑定父 ReActConfig，启用 InheritParentMiddlewares 的中间件继承。
// middleware inheritance for SubAgentSpecs with InheritParentMiddlewares.
// Call this after adding the middleware to config.Middlewares, but before
// calling NewReActAgent.
//
// Example:
//
//	mw := subagent.New(specs, &subagent.Config{EmitInternalEvents: true, MaxDepth: 5})
//	cfg.Middlewares = append(cfg.Middlewares, mw)
//	mw.Init(ctx, cfg)
//	agent := core.NewReActAgent(cfg)
func (m *SubAgentMiddleware) Init(ctx context.Context, config *core.ReActConfig[*schema.Message]) {
	m.parentCfg = config
	// ensureBuilt is deferred to ContributeTools (called during agent build).
}

// BindToConfig 将子 Agent 工具追加到 config.Tools（向后兼容）。
//
// Deprecated: Prefer using Init(ctx, config) or relying on the ToolContributor
// interface (which is automatically collected during agent build). BindToConfig
// has a timing dependency (must be called before NewReActAgent) and sets
// config.ToolsConfig = nil as a side effect.
//
// For each spec, it:
//  1. Builds the Agent from AgentConfig (if provided) or uses pre-built Agent.
//  2. Applies middleware inheritance if InheritParentMiddlewares is true.
//  3. Creates an AgentTool wrapper with MaxDepth and EmitInternalEvents.
//  4. Appends the tool to config.Tools.
//
// MUST be called before agent.Run().
// The ctx is used for sub-agent construction (AgentFactory calls, AgentTool wrapping).
// Pass the parent agent's build context or context.Background() if none is available.
func (m *SubAgentMiddleware) BindToConfig(ctx context.Context, config *core.ReActConfig[*schema.Message]) {
	m.mu.Lock()
	if m.built {
		m.mu.Unlock()
		return // idempotent
	}
	m.built = true
	m.mu.Unlock()

	m.ensureBuilt(ctx, config)
	config.Tools = append(config.Tools, m.tools...)
	config.ToolsConfig = nil
}

// ensureBuilt 带父配置构建全部 spec 并创建 AgentTool。
func (m *SubAgentMiddleware) ensureBuilt(ctx context.Context, config *core.ReActConfig[*schema.Message]) {
	for _, spec := range m.specs {
		agent := m.resolveAgent(ctx, spec, config)
		if agent == nil {
			continue
		}

		// Track this spec as successfully built
		m.builtInfos = append(m.builtInfos, &schema.ToolInfo{
			Name:        spec.Name,
			Description: spec.Description,
		})

		var toolOpts []core.AgentToolOption
		if m.cfg.EmitInternalEvents {
			toolOpts = append(toolOpts, core.WithEmitInternalEvents())
		}
		if m.cfg.MaxDepth > 0 {
			toolOpts = append(toolOpts, core.WithMaxDepth(m.cfg.MaxDepth))
		}
		tool := core.NewAgentTool(ctx, agent, toolOpts...)
		m.tools = append(m.tools, tool)
	}
}

// resolveAgent 解析 spec 得到 Agent；AgentConfig 优先于预构建 Agent。
// inheritance when requested.
//
// When both AgentConfig and Agent are set, AgentConfig takes precedence.
// When using a pre-built Agent with InheritParentMiddlewares, inheritance
// is NOT applied — middlewares are already fixed at construction time.
// Use AgentConfig instead when inheritance is needed.
func (m *SubAgentMiddleware) resolveAgent(ctx context.Context, spec SubAgentSpec, parentCfg *core.ReActConfig[*schema.Message]) core.Agent {
	// 1. Build from AgentConfig (takes precedence when both Agent and AgentConfig are set).
	if spec.AgentConfig != nil {
		cfg := m.buildConfig(spec, parentCfg)
		return core.NewReActAgent(cfg).
			WithName(spec.Name).
			WithDescription(spec.Description)
	}

	// 2. Use pre-built Agent.
	// Note: InheritParentMiddlewares is silently ignored for pre-built agents.
	// Middlewares are already fixed at Agent construction time.
	if spec.Agent != nil {
		return spec.Agent
	}

	// 3. Lazy factory (legacy path).
	if spec.AgentFactory != nil {
		agent, err := spec.AgentFactory(ctx)
		if err == nil && agent != nil {
			return agent
		}
	}

	return nil
}

// buildConfig 从 AgentConfig 构建 ReActConfig 并合并继承中间件。
// inheritance when InheritParentMiddlewares is true and parentCfg is non-nil.
func (m *SubAgentMiddleware) buildConfig(spec SubAgentSpec, parentCfg *core.ReActConfig[*schema.Message]) *core.ReActConfig[*schema.Message] {
	cfg := spec.AgentConfig
	subCfg := &core.ReActConfig[*schema.Message]{
		Model:         cfg.Model,
		Tools:         cfg.Tools,
		Instruction:   cfg.SystemPrompt,
		MaxIterations: cfg.MaxIterations,
	}

	// Apply middleware inheritance when parent config is available.
	if spec.InheritParentMiddlewares && parentCfg != nil {
		subCfg.Middlewares = m.inheritedMiddlewares(parentCfg, spec.ExcludedParentMiddlewareNames)
	}
	// Append sub-agent's own middlewares.
	subCfg.Middlewares = append(subCfg.Middlewares, cfg.Middlewares...)

	return subCfg
}

// inheritedMiddlewares 返回父中间件副本，排除 SubAgentMiddleware 自身。
//   - The SubAgentMiddleware itself (always excluded, prevents infinite recursion).
//   - Any middleware whose type name matches an entry in excludedNames.
//
// Reference semantics: middleware interface values are copied (pointers to the
// same underlying instances). Shared mutable state in middlewares affects both
// parent and sub-agent.
func (m *SubAgentMiddleware) inheritedMiddlewares(parentCfg *core.ReActConfig[*schema.Message], excludedNames []string) []core.ReActMiddleware {
	excluded := make(map[string]bool, len(excludedNames)+1)
	for _, n := range excludedNames {
		excluded[n] = true
	}

	var inherited []core.ReActMiddleware
	for _, mw := range parentCfg.Middlewares {
		if mw == nil {
			continue
		}
		// Always exclude the SubAgentMiddleware itself.
		if _, ok := mw.(subAgentMarker); ok {
			continue
		}
		// Check additional exclusions by type name.
		typeName := fmt.Sprintf("%T", mw)
		if excluded[typeName] {
			continue
		}
		inherited = append(inherited, mw)
	}
	return inherited
}

// BeforeModelRewrite 将已成功构建的子 Agent 工具信息注入 state.ToolInfos。
// so the LLM sees the sub-agents as available tools. Only tools that were
// successfully built in ensureBuilt are advertised.
func (m *SubAgentMiddleware) BeforeModelRewrite(ctx context.Context, state *core.ReActAgentState, mc *core.ModelContext) (context.Context, *core.ReActAgentState, error) {
	state.ToolInfos = append(state.ToolInfos, m.builtInfos...)
	return ctx, state, nil
}

// isSubAgentMiddleware 标记自身以便继承过滤时排除。
// during middleware inheritance filtering.
func (m *SubAgentMiddleware) isSubAgentMiddleware() {}

// ---- 编译期接口检查 ----

var _ core.ReActMiddleware = (*SubAgentMiddleware)(nil)

// 预构建 Agent 无法动态继承中间件；需继承时请用 AgentConfig。
