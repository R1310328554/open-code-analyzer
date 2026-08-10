package core

// react_agent.go — ReActAgent 核心实现：配置冻结、Run/Resume、中间件链、ToolsNode 与可选 GraphReAct 图引擎。


import (
	"context"
	"fmt"
	"ragflow/internal/harness/graph/checkpoint"
	"strings"
	"sync"
	"sync/atomic"

	"ragflow/internal/harness/core/internal"
	"ragflow/internal/harness/core/schema"
)

// ReActConfig TypedReActAgent 配置。
type ReActConfig[M MessageType] struct {
	Model              Model[M]
	Tools              []Tool
	Instruction        string
	MaxIterations      int
	Middlewares        []TypedReActMiddleware[M]
	RetryConfig        *TypedModelRetryConfig[M]
	FailoverConfig     *FailoverConfig[M]
	ReturnDirectly     map[string]bool
	OutputKey          string
	GenModelInput      TypedGenModelInput[M]
	StateModifier      StateModifier[M]
	ToolsConfig        *ToolsNodeConfig
	EmitInternalEvents bool
	// GraphReAct 启用基于 StateGraph/Pregel 的 ReAct 执行。
	// 为 true 时每次迭代作为图节点，支持 checkpoint/interrupt/resume。
	// 默认 false，使用 react_loop.go 中的 for 循环。
	// 默认走简单 for 循环路径。
	GraphReAct bool
	// GraphReActCheckpointer GraphReAct 模式的检查点存储。
	// nil 时不持久化 checkpoint，但仍可通过 WithInterrupts 中断。
	// 中断行为由 graph.WithInterrupts 控制。
	GraphReActCheckpointer checkpoint.BaseCheckpointer
	// GraphReActInterruptBefore 指定中断前节点名列表。
	// 默认 ["execute_tools"]，工具执行前暂停供人工审批。
	GraphReActInterruptBefore []string
}

func DefaultReActConfig[M MessageType]() *ReActConfig[M] {
	return &ReActConfig[M]{MaxIterations: 10, Instruction: internal.DefaultSystemPrompt}
}

// ReActAgentResumeData Resume 时可携带的历史修改器等数据。
type ReActAgentResumeData struct {
	HistoryModifier func(ctx context.Context, messages []Message) []Message
}

// ReActAgent 实现 ReAct（推理 + 行动）模式。
//
// 生产特性：
//   - freeze-once：首次 Run/Resume 后配置原子冻结
//   - ToolsNode 抽象与工具调用中间件链
//   - Enhanced Tool 四端点类型支持
//   - DeferredToolInfos 服务端工具搜索
//   - EmitInternalEvents 转发 AgentTool 事件
//   - AfterToolCallsHook 供 AgentLoop 集成
//   - ResumeWithData / HistoryModifier 定制恢复行为
//   - SetRunLocalValue 时 gob 可编码性检查
type ReActAgent[M MessageType] struct {
	name   string
	desc   string
	config *ReActConfig[M]

	once   sync.Once
	frozen uint32
	run    typedRunFunc[M]
	exeCtx *execContext
}

var _ ResumableAgent = &ReActAgent[*schema.Message]{}
var _ TypedResumableAgent[*schema.AgenticMessage] = &ReActAgent[*schema.AgenticMessage]{}

type TypedGenModelInput[M MessageType] func(ctx context.Context, instruction string, input *TypedAgentInput[M]) ([]M, error)

// StateModifier 在调用模型前变换 Agent 状态。
type StateModifier[M MessageType] func(ctx context.Context, state *TypedReActAgentState[M]) (*TypedReActAgentState[M], error)

func defaultGenModelInput(ctx context.Context, instruction string, input *AgentInput) ([]Message, error) {
	msgs := make([]Message, 0, len(input.Messages)+1)
	if instruction != "" {
		processed := resolveTemplate(instruction, ctx)
		msgs = append(msgs, schema.SystemMessage(processed))
	}
	msgs = append(msgs, input.Messages...)
	return msgs, nil
}

func resolveTemplate(tmpl string, ctx context.Context) string {
	s := getSession(ctx)
	if s == nil {
		return tmpl
	}
	result := tmpl
	for k, v := range s.Values {
		repl := fmt.Sprintf("{%s}", k)
		if sv, ok := v.(string); ok {
			result = strings.ReplaceAll(result, repl, sv)
		}
	}
	return result
}

func NewReActAgent[M MessageType](cfg *ReActConfig[M]) *ReActAgent[M] {
	if cfg == nil {
		cfg = DefaultReActConfig[M]()
	}
	a := &ReActAgent[M]{name: "react_agent", desc: "ReAct agent using a chat model", config: cfg}
	if cfg.ToolsConfig == nil && len(cfg.Tools) > 0 {
		cfg.ToolsConfig = &ToolsNodeConfig{Tools: cfg.Tools, ReturnDirectly: cfg.ReturnDirectly}
	}
	return a
}
func (a *ReActAgent[M]) WithName(n string) *ReActAgent[M]        { a.name = n; return a }
func (a *ReActAgent[M]) WithDescription(d string) *ReActAgent[M] { a.desc = d; return a }
func (a *ReActAgent[M]) Name(_ context.Context) string           { return a.name }
func (a *ReActAgent[M]) Description(_ context.Context) string    { return a.desc }
func (a *ReActAgent[M]) GetType() string                         { return "ReActAgent" }

// ---- 冻结机制 ----

func (a *ReActAgent[M]) IsFrozen() bool { return atomic.LoadUint32(&a.frozen) == 1 }

func (a *ReActAgent[M]) freeze() { atomic.StoreUint32(&a.frozen, 1) }

// ---- Run / Resume ----

func (a *ReActAgent[M]) Run(ctx context.Context, input *TypedAgentInput[M], opts ...RunOption) *AsyncIterator[*TypedAgentEvent[M]] {
	it, gen := NewAsyncIteratorPair[*TypedAgentEvent[M]]()
	go func() {
		defer func() {
			if r := recover(); r != nil {
				gen.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("panic: %v", r)})
			}
			gen.Close()
		}()
		runFunc := a.buildRunFunc(ctx)
		runFunc(ctx, &typedRunParams[M]{input: input, generator: gen})
		a.freeze()
	}()
	return it
}

func (a *ReActAgent[M]) Resume(ctx context.Context, info *ResumeInfo, opts ...RunOption) *AsyncIterator[*TypedAgentEvent[M]] {
	it, gen := NewAsyncIteratorPair[*TypedAgentEvent[M]]()
	go func() {
		defer func() {
			if r := recover(); r != nil {
				gen.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("panic: %v", r)})
			}
			gen.Close()
		}()
		if info.WasInterrupted {
			if s, ok := info.InterruptState.(*TypedReActAgentState[M]); ok {
				runFunc := a.buildRunFunc(ctx)
				params := &typedRunParams[M]{input: &TypedAgentInput[M]{Messages: s.Messages, EnableStreaming: info.EnableStreaming}, generator: gen, interruptState: s, resumeInfo: info}
				if info.ResumeData != nil {
					if rd, ok := info.ResumeData.(*ReActAgentResumeData); ok && rd.HistoryModifier != nil {
						params.historyModifier = rd.HistoryModifier
					}
				}
				runFunc(ctx, params)
				a.freeze()
				return
			}
		}
		gen.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("resume called but agent was not interrupted or state is invalid")})
	}()
	return it
}

// ---- 内部类型 ----

type typedRunFunc[M MessageType] func(ctx context.Context, p *typedRunParams[M])

type typedRunParams[M MessageType] struct {
	input              *TypedAgentInput[M]
	generator          *AsyncGenerator[*TypedAgentEvent[M]]
	interruptState     *TypedReActAgentState[M]
	resumeInfo         *ResumeInfo
	historyModifier    func(context.Context, []Message) []Message
	afterToolCallsHook func(ctx context.Context) error
}

// reActExecCtx 单次执行上下文：事件发送、取消、重试信号等。
// 供模型包装器与流式重试共享。
type reActExecCtx struct {
	generator          *AsyncGenerator[*TypedAgentEvent[*schema.Message]]
	cancelCtx          *cancelContext
	suppressEventSend  bool
	retrySignal        *retrySignal
	failoverLastModel  Model[*schema.Message]
	afterToolCallsHook func(ctx context.Context) error
}

func (ec *reActExecCtx) send(ev any) {
	if ec != nil && ec.generator != nil {
		if te, ok := ev.(*TypedAgentEvent[*schema.Message]); ok {
			ec.generator.Send(te)
		}
	}
}

type execContext struct {
	instruction        string
	returnDirectly     map[string]bool
	toolInfos          []*schema.ToolInfo // from config.Tools + contributor ToolInfos
	deferredToolInfos  []*schema.ToolInfo
	toolSearchTool     *schema.ToolInfo
	emitInternalEvents bool

	// ToolContributor 中间件贡献的工具（once.Do 收集一次）。
	contribTools          []Tool
	contribToolInfos      []*schema.ToolInfo
	contribReturnDirectly map[string]bool
}

// ---- Run 函数构建器 ----

func (a *ReActAgent[M]) buildRunFunc(ctx context.Context) typedRunFunc[M] {
	var onceRun typedRunFunc[M]
	a.once.Do(func() {
		ec, err := a.prepareExecContext(ctx)
		if err != nil {
			onceRun = func(_ context.Context, _ *typedRunParams[M]) {}
			a.run = onceRun
			return
		}
		a.exeCtx = ec
		// 判断是否有工具：config.Tools + Contributor 工具
		hasTools := len(a.config.Tools) > 0 ||
			(a.config.ToolsConfig != nil && len(a.config.ToolsConfig.Tools) > 0) ||
			len(ec.contribTools) > 0
		if !hasTools {
			onceRun = a.buildNoToolsRunFunc()
		} else if a.config.GraphReAct {
			onceRun = a.buildGraphReActRunFunc()
		} else {
			onceRun = a.buildReActRunFunc()
		}
		a.run = onceRun
	})
	return a.run
}

func (a *ReActAgent[M]) prepareExecContext(ctx context.Context) (*execContext, error) {
	instruction := a.config.Instruction
	if instruction == "" {
		instruction = internal.DefaultSystemPrompt
	}
	rd := a.config.ReturnDirectly
	if rd == nil {
		rd = make(map[string]bool)
	}

	// 从 ToolContributor 中间件收集工具与 ReturnDirectly。
	contribTools := collectContributorTools(ctx, a.config.Middlewares)
	contribInfos := collectContributorToolInfos(ctx, a.config.Middlewares)
	contribRD := collectContributorReturnDirectly(ctx, a.config.Middlewares)

	// 合并 ReturnDirectly 映射。
	mergedRD := make(map[string]bool, len(rd)+len(contribRD))
	for k, v := range rd {
		mergedRD[k] = v
	}
	for k, v := range contribRD {
		mergedRD[k] = v
	}

	// 合并 ToolInfo 时避免 Tools 与 ToolsConfig 重复来源。
	// ToolsConfig 为 nil 时 NewReActAgent 会用 config.Tools 填充。
	// 因此只选单一来源构建 baseInfos。
	var baseInfos []*schema.ToolInfo
	if a.config.ToolsConfig != nil && len(a.config.ToolsConfig.Tools) > 0 {
		baseInfos = toolsToInfosTyped[M](a.config.ToolsConfig.Tools)
	} else {
		baseInfos = toolsToInfosTyped[M](a.config.Tools)
	}
	allInfos := make([]*schema.ToolInfo, 0, len(baseInfos)+len(contribInfos))
	allInfos = append(allInfos, baseInfos...)
	allInfos = append(allInfos, contribInfos...)

	return &execContext{
		instruction:           instruction,
		returnDirectly:        mergedRD,
		toolInfos:             allInfos,
		contribTools:          contribTools,
		contribToolInfos:      contribInfos,
		contribReturnDirectly: contribRD,
		emitInternalEvents:    a.config.EmitInternalEvents,
	}, nil
}

// ---- 无工具 Run 路径 ----

func (a *ReActAgent[M]) buildNoToolsRunFunc() typedRunFunc[M] {
	return func(ctx context.Context, p *typedRunParams[M]) {
		// 合并 config.Tools 与 contribTools
		allTools := make([]Tool, 0, len(a.config.Tools)+len(a.exeCtx.contribTools))
		allTools = append(allTools, a.config.Tools...)
		allTools = append(allTools, a.exeCtx.contribTools...)

		// BeforeAgent 中间件链
		rc := &ReActAgentContext{Instruction: a.exeCtx.instruction, Tools: allTools, ReturnDirectly: a.exeCtx.returnDirectly}
		if err := a.runBeforeAgent(&ctx, rc, p.generator); err != nil {
			return
		}

		model := BuildModelWrapperChain(a.config.Model, nil, a.config, a.exeCtx.toolInfos)
		state := NewReActAgentState(p.input.Messages, a.exeCtx.toolInfos, a.config.MaxIterations)

		// BeforeModelRewrite 中间件链
		mc := &TypedModelContext[M]{Tools: state.ToolInfos, ModelRetryConfig: a.config.RetryConfig, ModelFailoverConfig: a.config.FailoverConfig}
		if err := a.runBeforeModelRewrite(&ctx, &state, mc, p.generator); err != nil {
			return
		}

		if a.config.StateModifier != nil {
			var err error
			state, err = a.config.StateModifier(ctx, state)
			if err != nil {
				p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("StateModifier: %w", err)})
				return
			}
		}

		modelMsgs := buildModelInputFromState[M](state.Messages, rc.Instruction)
		resp, err := model.Generate(ctx, modelMsgs)
		if err != nil {
			p.generator.Send(&TypedAgentEvent[M]{Err: err})
			return
		}
		p.generator.Send(typedModelOutputEvent(resp, nil))
		state.Messages = append(state.Messages, resp)

		// AfterModelRewrite 中间件链
		if err := a.runAfterModelRewrite(&ctx, &state, mc, p.generator); err != nil {
			return
		}

		if a.config.OutputKey != "" && !isNilMessage(resp) {
			setOutputToSession(ctx, resp, a.config.OutputKey)
		}

		// AfterAgent 中间件链
		a.runAfterAgent(&ctx, state, p.generator)
	}
}

// runBeforeAgent 执行 BeforeAgent 链；错误则终止。
// 任一中间件返回 error 则中止 Run。
func (a *ReActAgent[M]) runBeforeAgent(ctx *context.Context, rc *ReActAgentContext, gen *AsyncGenerator[*TypedAgentEvent[M]]) error {
	for _, mw := range a.config.Middlewares {
		if mw == nil {
			continue
		}
		var err error
		*ctx, rc, err = mw.BeforeAgent(*ctx, rc)
		if err != nil {
			gen.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("BeforeAgent: %w", err)})
			return err
		}
	}
	return nil
}

// runBeforeModelRewrite 执行 BeforeModelRewrite 链。
func (a *ReActAgent[M]) runBeforeModelRewrite(ctx *context.Context, state **TypedReActAgentState[M], mc *TypedModelContext[M], gen *AsyncGenerator[*TypedAgentEvent[M]]) error {
	for _, mw := range a.config.Middlewares {
		if mw == nil {
			continue
		}
		var err error
		*ctx, *state, err = mw.BeforeModelRewrite(*ctx, *state, mc)
		if err != nil {
			gen.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("BeforeModelRewrite: %w", err)})
			return err
		}
	}
	return nil
}

// runAfterModelRewrite 执行 AfterModelRewrite 链。
func (a *ReActAgent[M]) runAfterModelRewrite(ctx *context.Context, state **TypedReActAgentState[M], mc *TypedModelContext[M], gen *AsyncGenerator[*TypedAgentEvent[M]]) error {
	for _, mw := range a.config.Middlewares {
		if mw == nil {
			continue
		}
		var err error
		*ctx, *state, err = mw.AfterModelRewrite(*ctx, *state, mc)
		if err != nil {
			gen.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("AfterModelRewrite: %w", err)})
			return err
		}
	}
	return nil
}

// runAfterAgent 执行 AfterAgent 链。
func (a *ReActAgent[M]) runAfterAgent(ctx *context.Context, state *TypedReActAgentState[M], gen *AsyncGenerator[*TypedAgentEvent[M]]) {
	for _, mw := range a.config.Middlewares {
		if mw == nil {
			continue
		}
		var err error
		*ctx, err = mw.AfterAgent(*ctx, state)
		if err != nil {
			gen.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("AfterAgent: %w", err)})
			return
		}
	}
}

// ---- 辅助函数 ----

func buildModelInputFromState[M MessageType](messages []M, instruction string) []M {
	var msgs []M
	if instruction != "" {
		msgs = append(msgs, any(schema.SystemMessage(instruction)).(M))
	}
	for _, m := range messages {
		msgs = append(msgs, m)
	}
	return msgs
}

func setOutputToSession[M MessageType](ctx context.Context, msg M, key string) {
	if !isNilMessage(msg) {
		s := getSession(ctx)
		if s != nil {
			s.Values[key] = extractTextContent(msg)
		}
	}
}

func toolsToInfosTyped[M MessageType](tools []Tool) []*schema.ToolInfo {
	infos := make([]*schema.ToolInfo, 0, len(tools))
	for _, t := range tools {
		if p, ok := t.(ToolInfoProvider); ok {
			infos = append(infos, p.ToolInfo())
		} else {
			infos = append(infos, &schema.ToolInfo{Name: t.Name(), Description: t.Description()})
		}
	}
	return infos
}

func extractTextContent[M MessageType](msg M) string {
	switch v := any(msg).(type) {
	case *schema.Message:
		return v.Content
	case *schema.AgenticMessage:
		var texts []string
		for _, b := range v.ContentBlocks {
			if b.Type == "text" {
				texts = append(texts, b.Text)
			}
		}
		return strings.Join(texts, "\n")
	default:
		return ""
	}
}

// findTool 按名称查找工具。
func findTool(tools []Tool, name string) Tool {
	for _, t := range tools {
		if t.Name() == name {
			return t
		}
	}
	return nil
}

// extractToolCalls 从模型响应提取 ToolCalls。
// 支持 *schema.Message 与 *schema.AgenticMessage。
func extractToolCalls[M MessageType](resp M) []schema.ToolCall {
	switch v := any(resp).(type) {
	case *schema.Message:
		if len(v.ToolCalls) > 0 {
			return v.ToolCalls
		}
	case *schema.AgenticMessage:
		var tc []schema.ToolCall
		for _, b := range v.ContentBlocks {
			if b.Type == "tool_use" && b.ToolCall != nil && b.ToolCall.ID != "" && b.ToolCall.Name != "" {
				tc = append(tc, schema.ToolCall{
					ID:       b.ToolCall.ID,
					Function: schema.ToolCallFunction{Name: b.ToolCall.Name, Arguments: b.ToolCall.Arguments},
				})
			}
		}
		return tc
	}
	return nil
}

// streamWithCancel 包装流式调用以响应取消信号。
func streamWithCancel[M MessageType](s *schema.StreamReader[M], cc *cancelContext) *schema.StreamReader[M] {
	if cc == nil {
		return s
	}
	select {
	case <-cc.immediateChan:
		s.Close()
		r := schema.NewStreamReader[M]()
		var zero M
		r.Send(zero, ErrStreamCanceled)
		r.Close()
		return r
	default:
	}
	r := schema.NewStreamReader[M]()
	go func() {
		defer r.Close()
		defer s.Close()
		ch := make(chan struct {
			Data M
			Err  error
		}, 64)
		go func() {
			defer close(ch)
			for {
				select {
				case <-cc.immediateChan:
					return
				default:
				}
				d, e := s.Recv()
				select {
				case <-cc.immediateChan:
					return
				default:
				}
				select {
				case ch <- struct {
					Data M
					Err  error
				}{d, e}:
				case <-cc.immediateChan:
					return
				}
				if e != nil {
					return
				}
			}
		}()
		for {
			select {
			case <-cc.immediateChan:
				var z M
				r.Send(z, ErrStreamCanceled)
				return
			case v := <-ch:
				if v.Err != nil {
					return
				}
				r.Send(v.Data, nil)
			}
		}
	}()
	return r
}

// getChatModelExecCtx 从 context 取 reActExecCtx。
func getChatModelExecCtx(ctx context.Context) *reActExecCtx {
	rc := getRunCtx(ctx)
	if rc == nil {
		return nil
	}
	// 存储在 run session Values["__exec_ctx"]。
	if ec, ok := rc.Session.Values["__exec_ctx"].(*reActExecCtx); ok {
		return ec
	}
	return nil
}

// getReActExecCtx 类型化封装。
func getReActExecCtx[M MessageType](ctx context.Context) *reActExecCtx {
	return getChatModelExecCtx(ctx)
}

// CheckpointDataVersion checkpoint 数据格式版本。
type CheckpointDataVersion int

const CheckpointDataV1 CheckpointDataVersion = 1

// preprocessCheckpointData 恢复时向前兼容迁移。
func preprocessCheckpointData(data any) any { return data }

// WithGraphReAct 为 ReActConfig 启用图引擎 ReAct。
// 每次迭代为 StateGraph 节点，自动 checkpoint/interrupt/resume。
// 可选 MemorySaver 等 Checkpointer。
//
// Usage:
//
//	cfg := DefaultReActConfig[*schema.Message]()
//	cfg.GraphReAct = true
//	cfg.GraphReActCheckpointer = checkpoint.NewMemorySaver()  // optional
func WithGraphReAct[M MessageType](cfg *ReActConfig[M], cptr checkpoint.BaseCheckpointer) {
	cfg.GraphReAct = true
	cfg.GraphReActCheckpointer = cptr
}

// WithGraphReActInterrupt 自定义中断前节点列表。
// 默认 execute_tools，可定制人工审批点。
func WithGraphReActInterrupt[M MessageType](cfg *ReActConfig[M], interruptBefore ...string) {
	cfg.GraphReActInterruptBefore = interruptBefore
}

// ---- 基于图的 ReAct Run 路径 ----
//
// GraphReAct 启用时由 ReActGraph + Pregel 驱动循环。
// 每轮 superstep 提供：
//   - 节点边界自动 checkpoint
//   - 工具执行前 interrupt
//   - 同 config 从 checkpoint 恢复
//   - pregel.StreamManager 流式事件

func (a *ReActAgent[M]) buildGraphReActRunFunc() typedRunFunc[M] {
	return func(ctx context.Context, p *typedRunParams[M]) {
		// 图 ReAct 当前仅支持 *schema.Message。
		// AgenticMessage 回退到 for 循环实现。
		var zero M
		_, isMessage := any(zero).(*schema.Message)
		if !isMessage {
			// 非 Message 类型同样回退。
			a.buildReActRunFunc()(ctx, p)
			return
		}

		// 从 Agent 配置构建 ReActGraphConfig。
		graphCfg := &ReActGraphConfig{
			Checkpointer:    a.config.GraphReActCheckpointer,
			InterruptBefore: a.config.GraphReActInterruptBefore,
			RecursionLimit:  a.config.MaxIterations * 2, // 每轮约 2 个节点，RecursionLimit 留余量
		}

		// 类型断言为 *ReActAgent[*schema.Message]。
		msgAgent, ok := any(a).(*ReActAgent[*schema.Message])
		if !ok {
			p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("graph ReAct: agent type assertion failed")})
			return
		}

		rg, err := NewReActGraph(msgAgent, graphCfg, a.exeCtx.toolInfos)
		if err != nil {
			p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("NewReActGraph: %w", err)})
			return
		}

		// 构建 AgentInput。
		input := &AgentInput{Messages: messageSliceToAny2(p.input.Messages)}

		// 同步 Invoke 图。
		state, err := rg.Invoke(ctx, input, nil)
		if err != nil {
			p.generator.Send(&TypedAgentEvent[M]{Err: err})
			return
		}

		// 发送最终模型输出事件。
		if len(state.Messages) > 0 {
			last := state.Messages[len(state.Messages)-1]
			if !isNilMessage(last) {
				p.generator.Send(any(typedModelOutputEvent(last, nil)).(*TypedAgentEvent[M]))
			}
		}

		// 执行 afterToolCallsHook（若配置）。
		if p.afterToolCallsHook != nil {
			if err := p.afterToolCallsHook(ctx); err != nil {
				p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("after_tool_calls_hook: %w", err)})
			}
		}
	}
}

// messageSliceToAny2 将 []M 转为 []*schema.Message 供图使用。
func messageSliceToAny2[M MessageType](msgs []M) []*schema.Message {
	r := make([]*schema.Message, len(msgs))
	for i, m := range msgs {
		if msg, ok := any(m).(*schema.Message); ok {
			r[i] = msg
		} else {
			// 非 Message 项置 nil。
			r[i] = nil
		}
	}
	return r
}

// buildRunFunc 在 once.Do 中选择无工具/for 循环/GraphReAct 三条执行路径之一。
