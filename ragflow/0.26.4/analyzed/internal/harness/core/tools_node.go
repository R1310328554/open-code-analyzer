package core

// tools_node.go — ReAct 工具节点：解析 tool_calls、并发/串行调度、中间件链与 LoopGuard。


import (
	"context"
	"crypto/md5"
	"encoding/json"
	"fmt"
	"sync"

	"ragflow/internal/harness/core/schema"
)

// ToolsNodeConfig 配置 ReActAgent 的工具执行节点。
type ToolsNodeConfig struct {
	// Tools 可直接注册的工具列表。
	Tools []Tool

	// Registry 集中式工具注册表，优先加载后与 Tools 合并（同名以 Tools 为准）。
	// and filtering. When set, tools are loaded from the registry first,
	// then any tools in the Tools slice are added on top.
	Registry *ToolRegistry

	// ReturnDirectly 指定执行后立即退出的工具名。
	ReturnDirectly map[string]bool

	// ToolInvokeMiddlewares 基于 ToolInvocationContext 的中间件链。
	// Applied before tool execution in a chain (outermost first).
	ToolInvokeMiddlewares []ToolInvokeMiddleware

	// EmitInternalEvents 是否转发 AgentTool 子智能体的内部事件。
	EmitInternalEvents bool

	// LoopGuard 检测相同参数重复调用或连续失败以防死循环。
	// with identical arguments or consecutive failures. If nil, no guard is applied.
	LoopGuard *LoopGuard

	// UnknownToolHandler 处理未注册工具的调用。
	// If nil, an error message is returned to the model when a tool is not found.
	// The function receives the tool name and arguments JSON string.
	UnknownToolHandler func(ctx context.Context, name, arguments string) (string, error)

	// ArgumentsAliases 工具参数别名映射，执行前将别名键转为规范键。
	// Key = canonical tool name, value = map[canonicalArgumentKey][]alias.
	// When a tool call's JSON contains an alias key instead of the canonical key,
	// it is remapped before execution.
	// Example: {"get_weather": {"query": ["q", "search_term"]}}
	ArgumentsAliases map[string]map[string][]string
}

// ToolsNode 从模型输出提取 tool_calls、调度执行并收集结果。
// collecting results, and applying middleware chains.
type ToolsNode[M MessageType] struct {
	config  *ToolsNodeConfig
	toolMap map[string]Tool
}

// NewToolsNode 创建 ToolsNode 并构建 name→Tool 映射。
// Tools are loaded from the Registry first (if set), then any Tools slice
// entries are added on top (taking precedence on name conflict).
func NewToolsNode[M MessageType](cfg *ToolsNodeConfig) *ToolsNode[M] {
	tn := &ToolsNode[M]{config: cfg}
	capacity := len(cfg.Tools)
	if cfg.Registry != nil {
		capacity = max(capacity, len(cfg.Registry.tools))
	}
	tn.toolMap = make(map[string]Tool, capacity)
	if cfg.Registry != nil {
		for _, t := range cfg.Registry.AllTools() {
			tn.toolMap[t.Name()] = t
		}
	}
	for _, t := range cfg.Tools {
		tn.toolMap[t.Name()] = t
	}
	return tn
}

// Execute 处理模型响应中的全部 tool_calls；多调用时按能力分批并发/串行。
// It returns the list of tool result messages to append to state,
// and any agent action (e.g., return-directly) that should be handled.
//
// When multiple independent tool calls are present, Execute runs them concurrently
// using a bounded goroutine pool (default max concurrency = 10), reducing total
// latency from O(sum) to O(max). For a single tool call, no goroutine is spawned.
func (tn *ToolsNode[M]) Execute(ctx context.Context, resp M, state *TypedReActAgentState[M], _ interface{}) ([]M, *AgentAction, error) {
	toolCalls := extractToolCalls(resp)
	if len(toolCalls) == 0 {
		return nil, nil, nil
	}

	if len(toolCalls) == 1 {
		// Fast path: single tool call, no goroutine overhead.
		tc := toolCalls[0]
		var action *AgentAction
		if tn.config.ReturnDirectly != nil && tn.config.ReturnDirectly[tc.Function.Name] {
			action = NewExitAction()
		}
		toolMsg, err := tn.executeOne(ctx, tc)
		if err != nil {
			return nil, action, fmt.Errorf("tool '%s': %w", tc.Function.Name, err)
		}
		return []M{toolMsg}, action, nil
	}

	// Multi-tool path: plan execution batches by capability, then execute.
	batches := tn.planBatches(toolCalls)
	var action *AgentAction
	var mu sync.Mutex
	var firstErr error
	var results []M

	for _, batch := range batches {
		if batch.mode == batchParallel {
			// Execute parallel-safe tools concurrently.
			const maxConcurrency = 10
			sem := make(chan struct{}, maxConcurrency)
			parResults := make([]M, len(batch.calls))
			var wg sync.WaitGroup

			for i, tc := range batch.calls {
				if tn.config.ReturnDirectly != nil && tn.config.ReturnDirectly[tc.Function.Name] {
					mu.Lock()
					action = NewExitAction()
					mu.Unlock()
				}
				wg.Add(1)
				go func(idx int, call schema.ToolCall) {
					defer wg.Done()
					sem <- struct{}{}
					defer func() { <-sem }()
					msg, err := tn.executeOne(ctx, call)
					mu.Lock()
					defer mu.Unlock()
					if err != nil && firstErr == nil {
						firstErr = fmt.Errorf("tool '%s': %w", call.Function.Name, err)
						return
					}
					parResults[idx] = msg
				}(i, tc)
			}
			wg.Wait()
			for _, r := range parResults {
				if !isNilMessage(r) {
					results = append(results, r)
				}
			}
		} else {
			// Execute serial tools one by one.
			for _, tc := range batch.calls {
				if tn.config.ReturnDirectly != nil && tn.config.ReturnDirectly[tc.Function.Name] {
					action = NewExitAction()
				}
				msg, err := tn.executeOne(ctx, tc)
				if err != nil && firstErr == nil {
					firstErr = fmt.Errorf("tool '%s': %w", tc.Function.Name, err)
				}
				if !isNilMessage(msg) {
					results = append(results, msg)
				}
			}
		}
	}

	if firstErr != nil {
		return nil, action, firstErr
	}
	return results, action, nil
}

// executeOne 执行单次 tool_call，含 panic 恢复、LoopGuard 与中断检测。
func (tn *ToolsNode[M]) executeOne(ctx context.Context, tc schema.ToolCall) (msg M, err error) {
	// Panic recovery: tool.Invoke may panic, catch and convert to tool result message.
	defer func() {
		if r := recover(); r != nil {
			msg = tn.makeToolMsg(fmt.Sprintf("Error: tool '%s' panicked: %v", tc.Function.Name, r), tc.ID)
			err = nil // do not propagate Go error; captured as tool result text
		}
	}()

	// LoopGuard: detect repeated calls with identical arguments.
	if lg := tn.getLoopGuard(); lg != nil {
		if err := lg.CheckSameArgs(tc.Function.Name, tc.Function.Arguments); err != nil {
			return tn.makeToolMsg(fmt.Sprintf("Error: %v", err), tc.ID), nil
		}
	}

	tool, ok := tn.toolMap[tc.Function.Name]
	if !ok {
		if tn.config.UnknownToolHandler != nil {
			result, err := tn.config.UnknownToolHandler(ctx, tc.Function.Name, tc.Function.Arguments)
			if err != nil {
				return tn.makeToolMsg(fmt.Sprintf("Error: %v", err), tc.ID), nil
			}
			return tn.makeToolMsg(result, tc.ID), nil
		}
		errMsg := fmt.Sprintf("tool '%s' not found", tc.Function.Name)
		return tn.makeToolMsg(errMsg, tc.ID), nil
	}

	return tn.executeWithNewChain(ctx, tc, tool)
}

// executeWithNewChain 经中间件链调用工具并构造结果消息。
func (tn *ToolsNode[M]) executeWithNewChain(ctx context.Context, tc schema.ToolCall, tool Tool) (M, error) {
	// Remap argument aliases if configured.
	argsJSON := tc.Function.Arguments
	if aliases, ok := tn.config.ArgumentsAliases[tc.Function.Name]; ok && len(aliases) > 0 {
		argsJSON = remapToolArgs(tc.Function.Arguments, aliases)
	}

	args := &schema.ToolArgument{
		Name:      tc.Function.Name,
		Arguments: argsJSON,
		CallID:    tc.ID,
	}

	ictx := &ToolInvocationContext{
		Name:      tc.Function.Name,
		CallID:    tc.ID,
		Arguments: args,
	}

	var invokeFn InvokeTool
	if et, ok := tool.(EnhancedTool); ok {
		invokeFn = EnhancedToolToInvokeFn(et)
	} else {
		invokeFn = ToolToInvokeFn(tool)
	}

	chained := ToolWrapperChain(invokeFn, tn.config.ToolInvokeMiddlewares...)
	result, err := chained(ctx, ictx)
	if err != nil {
		// Detect tool-level interrupt: save state to context for resume.
		if tie, ok := IsToolInterrupt(err); ok {
			ctx = setToolInterruptState(ctx, tie)
			ctx = AppendAddressSegment(ctx, AddressSegmentTool, tc.ID)
			addr := getAddressSegments(ctx)
			addrCopy := make(Address, len(addr))
			copy(addrCopy, addr)
			return tn.makeToolMsg(fmt.Sprintf("[interrupted: %v]", tie.Info), tc.ID),
				&interruptResult{tie: tie, toolAddress: addrCopy}
		}
		return tn.makeToolMsg(fmt.Sprintf("Error: %v", err), tc.ID), nil
	}

	content := result.Content
	if result.Error != "" && content == "" {
		content = fmt.Sprintf("Error: %s", result.Error)
	}
	return tn.makeToolMsg(content, tc.ID), nil
}

// interruptResult 包装工具中断以便向上传播并保留恢复地址。
type interruptResult struct {
	tie         *ToolInterruptError
	toolAddress Address // address segments at time of interrupt; preserved for resume routing
}

func (e *interruptResult) Error() string { return fmt.Sprintf("interrupt: %v", e.tie.Info) }

// remapToolArgs 将 JSON 参数中的别名键替换为规范键。
func remapToolArgs(argsJSON string, aliases map[string][]string) string {
	if len(aliases) == 0 || argsJSON == "" {
		return argsJSON
	}
	var raw map[string]json.RawMessage
	if err := json.Unmarshal([]byte(argsJSON), &raw); err != nil {
		return argsJSON
	}
	changed := false
	for canonical, aliasList := range aliases {
		for _, alias := range aliasList {
			if v, ok := raw[alias]; ok {
				if _, exists := raw[canonical]; !exists {
					raw[canonical] = v
					delete(raw, alias)
					changed = true
				}
			}
		}
	}
	if !changed {
		return argsJSON
	}
	b, _ := json.Marshal(raw)
	return string(b)
}

func (tn *ToolsNode[M]) makeToolMsg(content, callID string) M {
	var zero M
	switch any(zero).(type) {
	case *schema.AgenticMessage:
		return any(&schema.AgenticMessage{
			Role:    schema.AgenticRoleUser,
			Content: content,
			ContentBlocks: []schema.ContentBlock{
				{Type: "tool_result", ToolResult: &schema.ToolResult{
					ToolCallID: callID, Content: content,
				}},
			},
		}).(M)
	default:
		return any(schema.ToolMessage(content, callID)).(M)
	}
}

// ---- 辅助：工具结果转事件 ----

func toolResultToEvent[M MessageType](msg M, roleName string) *TypedAgentEvent[M] {
	if m, ok := any(msg).(*schema.Message); ok {
		return any(typedEventFromMessage(m, nil, schema.RoleTool, roleName)).(*TypedAgentEvent[M])
	}
	return nil
}

// ---- JSON 辅助 ----

func parseToolArgs(argsJSON string, target any) error {
	if err := json.Unmarshal([]byte(argsJSON), target); err != nil {
		return fmt.Errorf("invalid tool arguments JSON: %w", err)
	}
	return nil
}

// ---- LoopGuard：检测相同参数重复调用 ----

// LoopGuard 跟踪同工具同参数连续次数与连续失败次数。
// with identical parameters. It tracks consecutive same-args calls per tool.
type LoopGuard struct {
	mu       sync.Mutex
	sameArgs map[string]int // key = toolName+"|"+argsHash
	failures map[string]int // key = toolName
	maxSame  int
	maxFails int
}

// NewLoopGuard 创建 LoopGuard，maxSame/maxFails 为 0 则禁用对应检测。
func NewLoopGuard(maxSame, maxFails int) *LoopGuard {
	return &LoopGuard{
		sameArgs: make(map[string]int),
		failures: make(map[string]int),
		maxSame:  maxSame,
		maxFails: maxFails,
	}
}

// CheckSameArgs 相同工具+参数超过阈值时返回错误。
func (g *LoopGuard) CheckSameArgs(toolName, argsJSON string) error {
	if g == nil || g.maxSame <= 0 {
		return nil
	}
	g.mu.Lock()
	defer g.mu.Unlock()
	hash := fmt.Sprintf("%s|%x", toolName, md5.Sum([]byte(argsJSON)))
	g.sameArgs[hash]++
	if g.sameArgs[hash] >= g.maxSame {
		return fmt.Errorf("loop guard: tool '%s' called %d times with identical arguments", toolName, g.sameArgs[hash])
	}
	return nil
}

// RecordFailure 记录工具连续失败次数。
// Returns an error if the failure limit is exceeded.
func (g *LoopGuard) RecordFailure(toolName string) error {
	if g == nil || g.maxFails <= 0 {
		return nil
	}
	g.mu.Lock()
	defer g.mu.Unlock()
	g.failures[toolName]++
	cnt := g.failures[toolName]
	if cnt >= g.maxFails {
		return fmt.Errorf("loop guard: tool '%s' failed %d consecutive times", toolName, cnt)
	}
	return nil
}

// Reset 成功或参数变化时清除该工具的跟踪状态。
func (g *LoopGuard) Reset(toolName string) {
	if g == nil {
		return
	}
	g.mu.Lock()
	defer g.mu.Unlock()
	// Remove all same-args entries for this tool
	for k := range g.sameArgs {
		if len(k) > len(toolName) && k[:len(toolName)] == toolName {
			delete(g.sameArgs, k)
		}
	}
	delete(g.failures, toolName)
}

// ---- 工具能力与批次规划 ----

// toolCapFromTool 返回工具能力，默认 ToolCapWritesFiles（保守串行）。
func toolCapFromTool(t Tool) ToolCapability {
	if ct, ok := t.(CapableTool); ok {
		return ct.Capability()
	}
	return ToolCapWritesFiles // default: conservative serial
}

// executionBatch 同一批次内的 tool_calls 共享并行或串行模式。
type executionBatch struct {
	mode  batchMode
	calls []schema.ToolCall
}

type batchMode int

const (
	batchParallel batchMode = iota
	batchSerial
)

// planBatches 只读工具合并并行批次，其余串行执行。
// Read-only tools are grouped for parallel execution; others run serially.
func (tn *ToolsNode[M]) planBatches(tcs []schema.ToolCall) []executionBatch {
	var batches []executionBatch
	var currentParallel []schema.ToolCall

	flushParallel := func() {
		if len(currentParallel) > 0 {
			batches = append(batches, executionBatch{mode: batchParallel, calls: currentParallel})
			currentParallel = nil
		}
	}

	for _, tc := range tcs {
		tool, ok := tn.toolMap[tc.Function.Name]
		if !ok {
			// Unknown tool - treat as serial to be safe.
			flushParallel()
			batches = append(batches, executionBatch{mode: batchSerial, calls: []schema.ToolCall{tc}})
			continue
		}
		cap := toolCapFromTool(tool)
		if cap == ToolCapReadOnly {
			currentParallel = append(currentParallel, tc)
		} else {
			flushParallel()
			batches = append(batches, executionBatch{mode: batchSerial, calls: []schema.ToolCall{tc}})
		}
	}
	flushParallel()
	return batches
}

// ---- executeOne 中的 LoopGuard 集成 ----

// getLoopGuard 返回配置中的 LoopGuard（共享指针状态）。
// It is stored on the ToolsNode to share state across invocation cycles.
func (tn *ToolsNode[M]) getLoopGuard() *LoopGuard {
	if tn.config != nil {
		return tn.config.LoopGuard
	}
	return nil
}

// clearLoopGuard writes the LoopGuard config back (no-op, config is shared by pointer).
func (tn *ToolsNode[M]) clearLoopGuard() {}

// 多工具并发默认上限 10；单工具走快速路径无 goroutine 开销。
