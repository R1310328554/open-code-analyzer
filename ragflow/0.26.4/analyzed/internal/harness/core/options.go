package core

// options.go — Agent Run 函数式选项：session、checkpoint、回调、取消与历史修饰。


import (
	"context"

	"ragflow/internal/harness/core/internal"
)

// RunOption 配置单次 Agent 运行行为。
type RunOption interface{ apply(*runOptions) }

type runOptions struct {
	sessionValues        map[string]any
	sharedParentSession  bool
	checkPointID         *string
	cancelCtx            *cancelContext
	skipTransferMessages bool
	agentNames           []string
	callbacks            []any
	afterToolCallsHook   func(ctx context.Context) error
	chatModelOptions     []ModelOption
	toolOptions          []ToolOption
	agentToolOptions     map[string][]RunOption
	historyModifier      func(context.Context, []Message) []Message
}

type runOptFn func(*runOptions)

func (f runOptFn) apply(o *runOptions) { f(o) }

func WrapImplSpecificOptFn(fn func(*runOptions)) RunOption {
	return runOptFn(fn)
}

// getCommonOptions 合并多个 RunOption 到 runOptions。
func getCommonOptions(o *runOptions, opts ...RunOption) *runOptions {
	if o == nil {
		o = &runOptions{}
	}
	for _, opt := range opts {
		if opt != nil {
			opt.apply(o)
		}
	}
	return o
}

// WithSessionValues 向运行上下文注入 session 级键值。
func WithSessionValues(vals map[string]any) RunOption {
	return runOptFn(func(o *runOptions) { o.sessionValues = vals })
}

// WithCheckPointID 设置 checkpoint ID，支持中断/恢复。
func WithCheckPointID(id string) RunOption {
	return runOptFn(func(o *runOptions) { o.checkPointID = &id })
}

// WithSkipTransferMessages 阻止接收父 Agent 转发的消息。
// from parent agents during a transfer.
func WithSkipTransferMessages() RunOption {
	return runOptFn(func(o *runOptions) { o.skipTransferMessages = true })
}

// WithCallbacks 注册 Agent 生命周期回调。
func WithCallbacks(cbs ...any) RunOption {
	return runOptFn(func(o *runOptions) { o.callbacks = cbs })
}

// WithAgentNames 将选项限定到指定 Agent 名称。
func WithAgentNames(names ...string) RunOption {
	return runOptFn(func(o *runOptions) { o.agentNames = names })
}

// WithSharedParentSession 子 Agent 可访问父 session 值。
func WithSharedParentSession() RunOption {
	return runOptFn(func(o *runOptions) { o.sharedParentSession = true })
}

// ---- 模型 Agent 专用选项 ----

// WithChatModelOptions 传递 temperature 等 Model 级选项。
func WithChatModelOptions(opts []ModelOption) RunOption {
	return WrapImplSpecificOptFn(func(o *runOptions) { o.chatModelOptions = opts })
}

// WithToolOptions 传递本次运行的工具调用选项。
func WithToolOptions(opts []ToolOption) RunOption {
	return WrapImplSpecificOptFn(func(o *runOptions) { o.toolOptions = opts })
}

// WithAgentToolOptions 为指定名称的子 Agent 传递 RunOption。
func WithAgentToolOptions(agentName string, opts []RunOption) RunOption {
	return WrapImplSpecificOptFn(func(o *runOptions) {
		if o.agentToolOptions == nil {
			o.agentToolOptions = make(map[string][]RunOption)
		}
		o.agentToolOptions[agentName] = opts
	})
}

// WithHistoryModifier 在每次 Model 调用前裁剪或变换消息历史。
// each model call. Useful for context-window management.
func WithHistoryModifier(fn func(context.Context, []Message) []Message) RunOption {
	return WrapImplSpecificOptFn(func(o *runOptions) { o.historyModifier = fn })
}

// WithAfterToolCallsHook 在本轮全部 tool 调用完成后、下次 Model 调用前同步执行。
// all tool calls in a react iteration complete, before the next Model call.
// 适用于 AgentLoop Push+Preempt：推送项需在下一轮 GenInput 可见。
// must be visible to the next turn's GenInput.
func WithAfterToolCallsHook(fn func(ctx context.Context) error) RunOption {
	return runOptFn(func(o *runOptions) { o.afterToolCallsHook = fn })
}

// ---- Agent 回调（可按名称限定）----

// WithAgentErrorCallback 注册不可恢复错误时的回调。
// It fires when an agent encounters a non-recoverable error during execution.
func WithAgentErrorCallback(fn func(ctx context.Context, err error)) RunOption {
	return WrapImplSpecificOptFn(func(o *runOptions) {
		o.callbacks = append(o.callbacks, callbackHandler{onError: fn})
	})
}

// WithAgentInterruptCallback 注册中断（如 human-in-the-loop）回调。
// It fires when the agent execution is interrupted (e.g., for human-in-the-loop).
func WithAgentInterruptCallback(fn func(ctx context.Context, info *InterruptInfo)) RunOption {
	return WrapImplSpecificOptFn(func(o *runOptions) {
		o.callbacks = append(o.callbacks, callbackHandler{onInterrupt: fn})
	})
}

// ---- 取消选项 ----

// WithCancel 返回 RunOption 与 AgentCancelFunc，配合 cancelContext 状态机。
func WithCancel() (RunOption, AgentCancelFunc) {
	cc := newCancelContext()
	opt := WrapImplSpecificOptFn(func(o *runOptions) { o.cancelCtx = cc })
	return opt, cc.buildCancelFunc()
}

// ---- 全局配置 ----

// SetLanguage 设置 Agent prompt 语言（English/Chinese）。
func SetLanguage(lang internal.Language) { internal.SetLanguage(lang) }

const (
	LanguageEnglish = internal.LanguageEnglish
	LanguageChinese = internal.LanguageChinese
)

// WrapImplSpecificOptFn 供 ReAct/Graph 等实现注入私有 runOptions 字段。
