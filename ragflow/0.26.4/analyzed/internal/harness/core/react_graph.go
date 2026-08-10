// react_graph.go — 图级 ReAct 循环：StateGraph 节点化 model_generate/execute_tools，集成 checkpoint 与 interrupt。
// 本文件将 ReAct 循环映射为 StateGraph，由 Pregel 引擎驱动。
// 每次迭代边界自动 checkpoint，支持工具执行前 interrupt。
// 替代 chatmodel_react.go 中的简单 for 循环。
//
// ReActGraph 将 ChatModelAgent 循环拆为图节点。
// graph.WithCheckpointer 与 WithInterrupts 在 superstep 边界生效。
// 完整中间件链与 ToolsNode 均保留。
// 支持 *schema.Message 与 *schema.AgenticMessage（类型注册）。
//
// 关键特性：
//   - model_generate / execute_tools 节点边界 checkpoint
//   - execute_tools 前 interrupt 供人工审批
//   - 从 checkpoint 恢复中断执行
//   - 完整 Before/After 中间件链
//   - ToolsNode 与 ToolCallMiddlewares 集成
//   - pregel.StreamManager 流式事件
//   - 泛型支持 Message 与 AgenticMessage
package core

import (
	"context"
	"errors"
	"fmt"

	"ragflow/internal/harness/core/schema"
	"ragflow/internal/harness/graph/channels"
	"ragflow/internal/harness/graph/checkpoint"
	"ragflow/internal/harness/graph/constants"
	"ragflow/internal/harness/graph/graph"
	"ragflow/internal/harness/graph/pregel"
	"ragflow/internal/harness/graph/types"
)

func init() {
	schema.RegisterType("_harness_react_graph_state", func() any { return &ReActGraphState{} })
}

// ReActGraphState 图级 ReAct 共享状态。
// 跨 superstep 持久化，支持 checkpoint 与 interrupt/resume。
type ReActGraphState struct {
	Messages       []*schema.Message
	ToolInfos      []*schema.ToolInfo
	IterationsLeft int
	MaxIterations  int
	AgentName      string
	Instruction    string
	HasToolCall    bool // 标记上一轮模型输出是否含 tool calls

	// ToolExecutedCache 缓存已完成 tool 结果，中断恢复时跳过重复执行。
	// 键为 tool call ID，值为结果内容。
	// 本 superstep 全部完成后清空。
	// 中断时经 Pregel checkpoint 持久化。
	// 恢复时跳过已缓存 tool（类似 Eino ToolsInterruptAndRerunExtra）。
	ToolExecutedCache map[string]string
}

// ReActGraph 将 ChatModelAgent 循环编译为 StateGraph。
// 每轮迭代 checkpoint，工具执行前可 interrupt。
type ReActGraph struct {
	compiled types.CompiledGraph
	config   *ReActConfig[*schema.Message]
	agent    *ReActAgent[*schema.Message]
	allInfos []*schema.ToolInfo // merged config + contributor tool infos
	allTools []Tool             // merged config + contributor tools
}

// ReActGraphConfig 构建 ReActGraph 的选项。
type ReActGraphConfig struct {
	Checkpointer    checkpoint.BaseCheckpointer
	InterruptBefore []string // 中断前节点名（默认 execute_tools）
	RecursionLimit  int
}

// NewReActGraph 构建含以下节点的 StateGraph：
//
//	prepare_input → model_generate → execute_tools → check_done
//	                                                ↘ [end]
//
// 默认在 execute_tools 设置 interrupt；有 Checkpointer 时每节点 transition 存 checkpoint。
// Pregel 引擎自动持久化状态。
//
// 图节点对应中间件：
//   - prepare_input：BeforeAgent
//   - model_generate：BeforeModelRewrite → 模型 → AfterModelRewrite
//   - check_done：AfterAgent
func NewReActGraph(agent *ReActAgent[*schema.Message], cfg *ReActGraphConfig, allToolInfos []*schema.ToolInfo) (*ReActGraph, error) {
	if cfg == nil {
		cfg = &ReActGraphConfig{}
	}
	agentCfg := agent.config
	// allToolInfos 为 nil 时从 Agent 工具推导
	// 确保模型包装器始终有 tool 元数据。
	if allToolInfos == nil {
		allToolInfos = toolsToInfosTyped[*schema.Message](agentCfg.Tools)
		if agentCfg.ToolsConfig != nil {
			allToolInfos = append(allToolInfos, toolsToInfosTyped[*schema.Message](agentCfg.ToolsConfig.Tools)...)
		}
	}
	sg := graph.NewStateGraph(&ReActGraphState{})

	// 为图引擎注册 state 字段 channel。
	sg.AddChannel("messages", channels.NewLastValue([]*schema.Message{}))
	sg.AddChannel("iterations_left", channels.NewLastValue(0))
	sg.AddChannel("has_tool_call", channels.NewLastValue(false))
	sg.AddChannel("tool_cache", channels.NewLastValue(map[string]string{}))

	// --- 节点：prepare_input ---
	// 启动时运行一次，执行 BeforeAgent。
	// 合并 config 与 contributor 的工具与 ReturnDirectly。
	allTools := make([]Tool, 0, len(agent.config.Tools))
	allTools = append(allTools, agent.config.Tools...)
	allRD := make(map[string]bool)
	for k, v := range agent.config.ReturnDirectly {
		allRD[k] = v
	}
	if ec := agent.exeCtx; ec != nil {
		allTools = append(allTools, ec.contribTools...)
		for k, v := range ec.contribReturnDirectly {
			allRD[k] = v
		}
	}

	sg.AddNode("prepare_input", func(ctx context.Context, state interface{}) (interface{}, error) {
		s := state.(*ReActGraphState)
		rc := &ReActAgentContext{
			Instruction:    s.Instruction,
			Tools:          allTools,
			ReturnDirectly: allRD,
		}
		for _, mw := range agentCfg.Middlewares {
			if mw == nil {
				continue
			}
			var err error
			ctx, rc, err = mw.BeforeAgent(ctx, rc)
			if err != nil {
				return nil, fmt.Errorf("BeforeAgent: %w", err)
			}
		}
		s.Instruction = rc.Instruction
		return s, nil
	})

	// --- 节点：model_generate ---
	// 用当前消息历史调用 LLM，执行 Before/AfterModelRewrite。
	// 每轮开始清空 ToolExecutedCache。
	// 递减 IterationsLeft 并检测 tool calls。
	sg.AddNode("model_generate", func(ctx context.Context, state interface{}) (interface{}, error) {
		s := state.(*ReActGraphState)
		if s.IterationsLeft <= 0 {
			return s, nil
		}
		s.IterationsLeft--
		// Clear tool cache at start of each iteration.
		s.ToolExecutedCache = nil

		model := BuildModelWrapperChain(agentCfg.Model, nil, agentCfg, allToolInfos)

		agentState := NewReActAgentState(
			messageSliceToAny(s.Messages),
			allToolInfos,
			s.IterationsLeft+1,
		)
		typedState := (*TypedReActAgentState[*schema.Message])(agentState)
		mc := &TypedModelContext[*schema.Message]{
			Tools:               allToolInfos,
			ModelRetryConfig:    agentCfg.RetryConfig,
			ModelFailoverConfig: agentCfg.FailoverConfig,
		}

		// BeforeModelRewrite 中间件链。
		for _, mw := range agentCfg.Middlewares {
			if mw == nil {
				continue
			}
			var err error
			ctx, typedState, err = mw.BeforeModelRewrite(ctx, typedState, mc)
			if err != nil {
				return nil, fmt.Errorf("BeforeModelRewrite: %w", err)
			}
		}
		s.Messages = typedState.Messages

		// StateModifier 钩子（如上下文裁剪）。
		if agentCfg.StateModifier != nil {
			var err error
			typedState, err = agentCfg.StateModifier(ctx, typedState)
			if err != nil {
				return nil, fmt.Errorf("StateModifier: %w", err)
			}
			s.Messages = typedState.Messages
		}

		// GenModelInput 或默认方式构建模型输入。
		var modelMsgs []*schema.Message
		if agentCfg.GenModelInput != nil {
			var err error
			modelMsgs, err = agentCfg.GenModelInput(ctx, s.Instruction,
				&TypedAgentInput[*schema.Message]{Messages: s.Messages})
			if err != nil {
				return nil, fmt.Errorf("GenModelInput: %w", err)
			}
		} else {
			modelMsgs = buildModelInputFromState(s.Messages, s.Instruction)
		}

		// 调用模型 Generate。
		resp, err := model.Generate(ctx, modelMsgs)
		if err != nil {
			return nil, fmt.Errorf("model: %w", err)
		}
		s.Messages = append(s.Messages, resp)

		// AfterModelRewrite 中间件链。
		typedState.Messages = s.Messages
		for _, mw := range agentCfg.Middlewares {
			if mw == nil {
				continue
			}
			var err error
			ctx, typedState, err = mw.AfterModelRewrite(ctx, typedState, mc)
			if err != nil {
				return nil, fmt.Errorf("AfterModelRewrite: %w", err)
			}
		}
		s.Messages = typedState.Messages

		// 检测响应是否含 tool calls。
		toolCalls := extractToolCalls(resp)
		s.HasToolCall = len(toolCalls) > 0

		return s, nil
	})

	// --- 节点：execute_tools ---
	// 用 ToolsNode 执行最后一条 assistant 消息中的 tool calls。
	// ToolExecutedCache 支持 interrupt/resume。
	// 中断时已完成的 tool 结果写入 cache。
	// 恢复时跳过 cache 中已有 ID。
	sg.AddNode("execute_tools", func(ctx context.Context, state interface{}) (interface{}, error) {
		s := state.(*ReActGraphState)
		if len(s.Messages) == 0 {
			return s, nil
		}
		last := s.Messages[len(s.Messages)-1]
		toolCalls := extractToolCalls(last)
		if len(toolCalls) == 0 {
			return s, nil
		}

		// 恢复或初始化 tool 执行 cache。
		cache := s.ToolExecutedCache
		if cache == nil {
			cache = make(map[string]string)
		}

		// 过滤已缓存（已完成）的 tool calls。
		var pendingCalls []schema.ToolCall
		for _, tc := range toolCalls {
			if _, done := cache[tc.ID]; !done {
				pendingCalls = append(pendingCalls, tc)
			}
		}
		if len(pendingCalls) == 0 {
			return s, nil
		}

		agentState := NewReActAgentState(
			messageSliceToAny(s.Messages),
			s.ToolInfos,
			s.IterationsLeft,
		)
		typedState := (*TypedReActAgentState[*schema.Message])(agentState)

		// 从 ToolsConfig 构建 ToolsNode，overlay allTools。
		// 保留 ToolInvokeMiddlewares 等配置。
		tnCfg := &ToolsNodeConfig{}
		if agentCfg.ToolsConfig != nil {
			*tnCfg = *agentCfg.ToolsConfig
		}
		tnCfg.Tools = allTools
		tnCfg.ReturnDirectly = allRD
		tn := NewToolsNode[*schema.Message](tnCfg)

		// 逐个执行 pending tool call 以便追踪结果。
		// 每次仅用单 tool call 消息简化追踪。
		var firstErr error
		var toolInterrupted bool
		for _, tc := range pendingCalls {
			// 构造仅含单个 tool call 的 assistant 消息。
			singleMsg := &schema.Message{
				Role:      schema.RoleAssistant,
				Content:   "",
				ToolCalls: []schema.ToolCall{tc},
			}
			var action *AgentAction
			var toolResults []*schema.Message
			toolResults, action, firstErr = tn.Execute(ctx, singleMsg, typedState, nil)
			if firstErr != nil {
				// 区分 tool interrupt 与真实错误。
				var ir *interruptResult
				if errors.As(firstErr, &ir) {
					toolInterrupted = true
					firstErr = nil
					// interrupt 时仍将 tool 消息写入 state 并缓存。
					for _, tr := range toolResults {
						s.Messages = append(s.Messages, tr)
						if tr != nil && tr.Content != "" {
							cache[tc.ID] = tr.Content
						}
					}
					break
				}
				// 真实错误则中止。
				break
			}
			for _, tr := range toolResults {
				s.Messages = append(s.Messages, tr)
				if tr != nil && tr.Content != "" {
					cache[tc.ID] = tr.Content
				}
			}
			if action != nil && action.Exit {
				s.IterationsLeft = 0
				s.HasToolCall = false
				break
			}
		}

		if firstErr != nil {
			s.ToolExecutedCache = cache
			return s, fmt.Errorf("tools: %w", firstErr)
		}

		if toolInterrupted {
			// 中断时保存 cache 并正常返回以便 checkpoint。
			s.ToolExecutedCache = cache
			return s, nil
		}

		// 全部成功则清空 cache 进入下一轮。
		s.ToolExecutedCache = nil
		return s, nil
	})

	// --- 节点：check_done ---
	// 执行 AfterAgent 并将 OutputKey 写入 session。
	sg.AddNode("check_done", func(ctx context.Context, state interface{}) (interface{}, error) {
		s := state.(*ReActGraphState)
		agentState := NewReActAgentState(
			messageSliceToAny(s.Messages),
			s.ToolInfos,
			s.IterationsLeft,
		)
		typedState := (*TypedReActAgentState[*schema.Message])(agentState)

		for _, mw := range agentCfg.Middlewares {
			if mw == nil {
				continue
			}
			var err error
			ctx, err = mw.AfterAgent(ctx, typedState)
			if err != nil {
				return nil, fmt.Errorf("AfterAgent: %w", err)
			}
		}

		// 若配置 OutputKey 则存储最终输出。
		if agentCfg.OutputKey != "" && len(s.Messages) > 0 {
			last := s.Messages[len(s.Messages)-1]
			setOutputToSession(ctx, last, agentCfg.OutputKey)
		}
		return s, nil
	})

	// --- 边 ---
	sg.AddEdge(constants.Start, "prepare_input")
	sg.AddEdge("prepare_input", "model_generate")

	// 条件边：无 tool call 或迭代耗尽 → check_done，否则 → execute_tools
	sg.AddConditionalEdges("model_generate", func(ctx context.Context, state interface{}) (interface{}, error) {
		s := state.(*ReActGraphState)
		if s.IterationsLeft <= 0 || !s.HasToolCall {
			return "check_done", nil
		}
		return "execute_tools", nil
	}, map[string]string{
		"check_done":    "check_done",
		"execute_tools": "execute_tools",
	})

	sg.AddEdge("execute_tools", "model_generate") // 执行工具后回到 model_generate
	sg.AddEdge("check_done", constants.End)       // 终止节点

	// --- 编译：checkpoint + interrupt ---
	interrupts := cfg.InterruptBefore
	if len(interrupts) == 0 {
		interrupts = []string{"execute_tools"}
	}
	rl := cfg.RecursionLimit
	if rl <= 0 {
		rl = constants.DefaultRecursionLimit
	}

	var compileOpts []interface{}
	compileOpts = append(compileOpts, graph.WithRecursionLimit(rl))
	if cfg.Checkpointer != nil {
		compileOpts = append(compileOpts, graph.WithCheckpointer(cfg.Checkpointer))
	}
	for _, name := range interrupts {
		compileOpts = append(compileOpts, graph.WithInterrupts(name))
	}

	compiled, err := sg.Compile(compileOpts...)
	if err != nil {
		return nil, fmt.Errorf("compile ReAct graph: %w", err)
	}

	return &ReActGraph{
		compiled: compiled,
		config:   agentCfg,
		agent:    agent,
		allInfos: allToolInfos,
		allTools: allTools,
	}, nil
}

// Invoke 经 Pregel 同步运行图级 ReAct。
// input 为 nil 时从 checkpoint 恢复（resume 路径）。
// buildInitialState 返回 nil 交由引擎处理。
func (rg *ReActGraph) Invoke(ctx context.Context, input *AgentInput, config *types.RunnableConfig) (*ReActGraphState, error) {
	var state interface{}
	if input != nil {
		state = rg.buildInitialState(input)
	}

	result, err := rg.compiled.Invoke(ctx, state, config)
	if err != nil {
		return nil, err
	}
	outState, ok := result.(*ReActGraphState)
	if !ok {
		return nil, fmt.Errorf("unexpected result type %T from graph", result)
	}
	return outState, nil
}

// Stream 返回 Pregel 流式事件 channel。
// outputCh 产出 checkpoint/task/values 等 StreamEvent。
// errCh 传递运行错误。
func (rg *ReActGraph) Stream(ctx context.Context, input *AgentInput, config *types.RunnableConfig, mode types.StreamMode) (<-chan interface{}, <-chan error) {
	state := rg.buildInitialState(input)
	return rg.compiled.Stream(ctx, state, mode, config)
}

// Resume 从 checkpoint 恢复中断的图执行。
func (rg *ReActGraph) Resume(ctx context.Context, config *types.RunnableConfig) (*ReActGraphState, error) {
	// 需传入 RunnableConfig 以定位 checkpoint。
	result, err := rg.compiled.Invoke(ctx, nil, config)
	if err != nil {
		return nil, err
	}
	outState, ok := result.(*ReActGraphState)
	if !ok {
		return nil, fmt.Errorf("unexpected result type %T from resumed graph", result)
	}
	return outState, nil
}

// ResumeStream 流式恢复。
func (rg *ReActGraph) ResumeStream(ctx context.Context, config *types.RunnableConfig, mode types.StreamMode) (<-chan interface{}, <-chan error) {
	return rg.compiled.Stream(ctx, nil, mode, config)
}

// Compile 返回底层 CompiledGraph。
func (rg *ReActGraph) Compile() types.CompiledGraph { return rg.compiled }

// ---- 辅助 ----

func (rg *ReActGraph) buildInitialState(input *AgentInput) *ReActGraphState {
	maxIter := rg.config.MaxIterations
	if maxIter <= 0 {
		maxIter = 10
	}
	state := &ReActGraphState{
		Messages:       input.Messages,
		IterationsLeft: maxIter,
		MaxIterations:  maxIter,
		AgentName:      rg.agent.name,
		Instruction:    rg.config.Instruction,
	}
	// 使用合并后的 tool infos。
	state.ToolInfos = make([]*schema.ToolInfo, len(rg.allInfos))
	copy(state.ToolInfos, rg.allInfos)
	return state
}

func messageSliceToAny(msgs []*schema.Message) []Message {
	r := make([]Message, len(msgs))
	for i, m := range msgs {
		r[i] = m
	}
	return r
}

// 确保 pregel 包 side-effect 导入（引擎注册）。
var _ = pregel.Engine{}

// 图拓扑：Start → prepare_input → model_generate ⇄ execute_tools → check_done → End。
