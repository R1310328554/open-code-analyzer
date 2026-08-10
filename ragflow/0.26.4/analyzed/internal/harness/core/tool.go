package core

// tool.go — AgentTool：将 Agent 包装为 Tool，支持嵌套深度限制与内部事件转发。


import (
	"context"
	"fmt"

	"ragflow/internal/harness/core/schema"
)

// subAgentDepthKey 上下文键，跟踪嵌套 AgentTool 的递归深度。
// 值为 int，表示当前嵌套层级。
type subAgentDepthKey struct{}

// AgentToolOptions AgentTool 行为配置。
type AgentToolOptions struct {
	FullChatHistoryAsInput bool
	EmitInternalEvents     bool // Forward inner agent's events to parent stream
	MaxDepth               int  // 0 = unlimited sub-agent nesting depth. Set via WithMaxDepth.
}

// AgentToolOption AgentTool 配置选项函数。
type AgentToolOption func(*AgentToolOptions)

// WithFullChatHistoryAsInput 使用完整聊天历史作为内部智能体输入。
func WithFullChatHistoryAsInput() AgentToolOption {
	return func(o *AgentToolOptions) { o.FullChatHistoryAsInput = true }
}

// WithEmitInternalEvents 启用内部事件转发（不写入父 runSession）。
// 中断经 CompositeInterrupt 传播；Exit/Transfer/BreakLoop 限定在工具边界内。
// agent output to the end user via Runner.
//
// Action Scoping:
//   - Interrupted actions are propagated via CompositeInterrupt for proper interrupt/resume
//   - Exit, TransferToAgent, BreakLoop actions are scoped to the agent tool boundary (ignored outside)
//
// Note: These forwarded events are NOT recorded in the parent agent's runSession.
// They are only emitted to the end-user and have no effect on the parent agent's state or checkpoint.
func WithEmitInternalEvents() AgentToolOption {
	return func(o *AgentToolOptions) { o.EmitInternalEvents = true }
}

// WithMaxDepth 设置最大嵌套深度以防无限递归。
// When set (>=1), AgentTool checks a depth counter in the context before executing
// the inner agent. If the current depth >= maxDepth, the call returns an error.
// Default: 0 (no limit).
func WithMaxDepth(d int) AgentToolOption {
	return func(o *AgentToolOptions) { o.MaxDepth = d }
}

// NewAgentTool 将 Agent 包装为 Tool 供其他智能体调用。
// The agent must have non-empty Name and Description, used as the tool name/description.
//
// Action Scoping:
//   - Exit, TransferToAgent, BreakLoop actions from the inner agent are ignored outside the tool
//   - Interrupted actions are propagated via CompositeInterrupt for proper interrupt/resume
func NewAgentTool(ctx context.Context, agent Agent, options ...AgentToolOption) Tool {
	opts := &AgentToolOptions{}
	for _, o := range options {
		o(opts)
	}
	name := agent.Name(ctx)
	if name == "" {
		name = "agent_tool"
	}
	desc := agent.Description(ctx)
	return &agentTool{
		name: name, desc: desc, agent: agent,
		opts: opts, baseCtx: ctx,
	}
}

type agentTool struct {
	name    string
	desc    string
	agent   Agent
	opts    *AgentToolOptions
	baseCtx context.Context
}

func (t *agentTool) Name() string        { return t.name }
func (t *agentTool) Description() string { return t.desc }

// Invoke 运行内部智能体，收集助手输出；超深度或中断时返回错误。
func (t *agentTool) Invoke(ctx context.Context, args string, opts ...ToolOption) (result string, err error) {
	// Panic recovery: runner.Run or iter.Next may panic; catch and convert to Go error.
	defer func() {
		if r := recover(); r != nil {
			err = fmt.Errorf("agent tool '%s' panicked: %v", t.name, r)
			result = ""
		}
	}()

	// Derive sub-agent run context from the invocation context to propagate
	// cancellation/deadline. Construction-time baseCtx values (e.g. recursion
	// depth guard) are preserved by adding them to the derived context.
	runCtx := ctx
	if t.baseCtx != nil {
		runCtx = context.WithValue(ctx, subAgentDepthKey{}, 0) // overridden below
	}

	// Recursion depth guard — always propagate the depth counter so nested
	// AgentTool invocations see the correct nesting level regardless of which
	// middleware created them.
	currentDepth := 0
	if v := ctx.Value(subAgentDepthKey{}); v != nil {
		currentDepth = v.(int)
	}
	if t.opts.MaxDepth > 0 && currentDepth >= t.opts.MaxDepth {
		return "", fmt.Errorf("agent tool '%s': recursion limit exceeded (max depth: %d)", t.name, t.opts.MaxDepth)
	}
	// Always increment — even when MaxDepth=0 — so nested calls see real depth.
	runCtx = context.WithValue(runCtx, subAgentDepthKey{}, currentDepth+1)

	runner := NewTypedRunner(RunnerConfig[*schema.Message]{Agent: t.agent})
	messages := []Message{schema.UserMessage(args)}
	if t.opts.FullChatHistoryAsInput {
		if ec := getChatModelExecCtx(ctx); ec != nil {
			// TODO: extract full chat history from parent execution context
		}
	}

	iter := runner.Run(runCtx, messages)

	// EmitInternalEvents — read from parent ctx (ctx), not runCtx, because
	// the runCtx is the sub-agent's independent context and has no parent execCtx.
	var parentEC *reActExecCtx
	if t.opts.EmitInternalEvents {
		parentEC = getChatModelExecCtx(ctx)
	}

	var interrupted bool
	for {
		ev, ok := iter.Next()
		if !ok {
			break
		}
		if ev.Err != nil {
			return "", fmt.Errorf("agent tool '%s': %w", t.name, ev.Err)
		}

		// EmitInternalEvents: forward events to parent stream
		if parentEC != nil && t.opts.EmitInternalEvents {
			parentEC.send(ev)
		}

		if ev.Action != nil && ev.Action.Interrupted != nil {
			interrupted = true
			result += fmt.Sprintf("[interrupted: %v]", ev.Action.Interrupted.Data)
			break
		}
		if ev.Action != nil && (ev.Action.Exit || ev.Action.TransferToAgent != nil || ev.Action.BreakLoop != nil) {
			// Scoped: these actions are for the inner agent only, not propagated
			continue
		}
		if ev.Output != nil && ev.Output.MessageOutput != nil {
			if !ev.Output.MessageOutput.IsStreaming && ev.Output.MessageOutput.Message != nil {
				msg := ev.Output.MessageOutput.Message
				if msg.Role == schema.RoleAssistant {
					result += msg.Content
				}
			}
		}
	}
	if interrupted {
		return result, fmt.Errorf("agent tool '%s' was interrupted", t.name)
	}
	return result, nil
}

// Stream 调用 Invoke 并将结果包装为字符串流。
func (t *agentTool) Stream(ctx context.Context, args string, opts ...ToolOption) (*schema.StreamReader[string], error) {
	r, err := t.Invoke(ctx, args, opts...)
	if err != nil {
		return nil, err
	}
	return schema.StreamReaderFromArray([]string{r}), nil
}

// 内部事件转发仅面向终端用户，不影响父智能体状态或检查点。
