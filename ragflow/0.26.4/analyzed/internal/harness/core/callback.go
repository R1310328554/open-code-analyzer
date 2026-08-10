// callback.go — Agent 回调注册、上下文传播与 RunLocal 会话值读写。

package core

import (
	"context"
	"encoding/gob"
	"fmt"
	"io"
	"reflect"
	"slices"
)

// AgentCallbackInput Agent 回调 OnStart 的输入。
type AgentCallbackInput struct {
	Input      *AgentInput
	ResumeInfo *ResumeInfo
}

// AgentCallbackOutput Agent 回调 OnEnd 的输出。
type AgentCallbackOutput struct {
	Events *AsyncIterator[*AgentEvent]
}

type TypedAgentCallbackInput[M MessageType] struct {
	Input      *TypedAgentInput[M]
	ResumeInfo *ResumeInfo
}

type TypedAgentCallbackOutput[M MessageType] struct {
	Events *AsyncIterator[*TypedAgentEvent[M]]
}

// callbackHandler 注册的回调函数集合。
type callbackHandler struct {
	onStart     func(ctx context.Context, input *AgentCallbackInput)
	onEnd       func(ctx context.Context, output *AgentCallbackOutput)
	onError     func(ctx context.Context, err error)
	onInterrupt func(ctx context.Context, info *InterruptInfo)
}

type callbackKey struct{}

// getCallbacks 从 ctx 读取已注册的 callbackHandler 列表。
func getCallbacks(ctx context.Context) []callbackHandler {
	if v := ctx.Value(callbackKey{}); v != nil {
		return v.([]callbackHandler)
	}
	return nil
}

// propagateCallbacks 将父 ctx 中的回调复制到嵌套 RunOption。
func propagateCallbacks(ctx context.Context, opts []RunOption) []RunOption {
	cbs := getCallbacks(ctx)
	if len(cbs) == 0 {
		return opts
	}
	cbOpts := make([]RunOption, 0, len(cbs))
	for _, cb := range cbs {
		handler := cb
		wrapped := callbackHandler{onStart: handler.onStart, onEnd: handler.onEnd, onError: handler.onError, onInterrupt: handler.onInterrupt}
		cbOpts = append(cbOpts, WrapImplSpecificOptFn(func(o *runOptions) {
			o.callbacks = append(o.callbacks, wrapped)
		}))
	}
	return append(cbOpts, opts...)
}

// withCallbacks 将回调列表写入 ctx。
func withCallbacks(ctx context.Context, cbs []callbackHandler) context.Context {
	if len(cbs) == 0 {
		return ctx
	}
	return context.WithValue(ctx, callbackKey{}, cbs)
}

// initAgentCallbacks 从 RunOption 提取 callbackHandler 并注入 ctx。
func initAgentCallbacks(ctx context.Context, name, agentType string, opts ...RunOption) context.Context {
	o := getCommonOptions(nil, opts...)
	if len(o.callbacks) == 0 {
		return ctx
	}
	cbs := make([]callbackHandler, 0, len(o.callbacks))
	for _, cb := range o.callbacks {
		switch c := cb.(type) {
		case callbackHandler:
			cbs = append(cbs, c)
		}
	}
	return withCallbacks(ctx, cbs)
}

func initAgenticCallbacks(ctx context.Context, name, agentType string, opts ...RunOption) context.Context {
	return initAgentCallbacks(ctx, name, agentType, opts...)
}

func filterOptions(name string, opts []RunOption) []RunOption {
	// filterOptions 过滤 agentNames 不包含 name 的回调选项
	o := getCommonOptions(nil, opts...)
	if len(o.agentNames) == 0 {
		return opts
	}

	var filtered []RunOption
	for _, opt := range opts {
		// Filter out AgentNames options that don't match
		if fn, ok := opt.(runOptFn); ok {
			tmp := &runOptions{}
			fn(tmp)
			if tmp.agentNames != nil {
				if !slices.Contains(tmp.agentNames, name) {
					continue
				}
			}
		}
		filtered = append(filtered, opt)
	}
	return filtered
}

func filterCancelOption(opts []RunOption) []RunOption {
	// filterCancelOption 移除子 Agent 选项中的 cancel ctx，避免重复处理
	// to avoid duplicate cancel handling
	var filtered []RunOption
	for _, opt := range opts {
		if fn, ok := opt.(runOptFn); ok {
			tmp := &runOptions{}
			fn(tmp)
			if tmp.cancelCtx != nil {
				continue
			}
		}
		filtered = append(filtered, opt)
	}
	if len(filtered) == len(opts) {
		return opts
	}
	return filtered
}

func filterCallbackHandlersForNestedAgents(name string, opts []RunOption) []RunOption {
	// filterCallbackHandlersForNestedAgents 过滤非目标 Agent 的回调处理器
	o := getCommonOptions(nil, opts...)
	if len(o.agentNames) == 0 {
		return opts
	}

	var filtered []RunOption
	for _, opt := range opts {
		if fn, ok := opt.(runOptFn); ok {
			tmp := &runOptions{}
			fn(tmp)
			if tmp.agentNames != nil {
				if !slices.Contains(tmp.agentNames, name) {
					continue
				}
			}
		}
		filtered = append(filtered, opt)
	}
	return filtered
}

// getAgentType 读取 Agent 类型，默认 ReActAgent。
func getAgentType(a Agent) string {
	if t, ok := a.(interface{ GetType() string }); ok {
		return t.GetType()
	}
	return "ReActAgent"
}

// ---- Run 局部会话值辅助函数 ----

// SetRunLocalValue 写入 session.Values 并校验 gob 可序列化。
func SetRunLocalValue(ctx context.Context, key string, val any) error {
	// P2：Set 时检查 gob 可编码性，提前发现未注册类型
	if err := checkGobEncodability(key, val); err != nil {
		return err
	}

	rc := getRunCtx(ctx)
	if rc == nil || rc.Session == nil {
		return errNotInAgentExec
	}
	rc.Session.Values[key] = val
	return nil
}

// GetRunLocalValue 读取 session 局部值。
func GetRunLocalValue(ctx context.Context, key string) (any, bool, error) {
	rc := getRunCtx(ctx)
	if rc == nil || rc.Session == nil {
		return nil, false, errNotInAgentExec
	}
	v, ok := rc.Session.Values[key]
	return v, ok, nil
}

// DeleteRunLocalValue 删除 session 局部值。
func DeleteRunLocalValue(ctx context.Context, key string) error {
	rc := getRunCtx(ctx)
	if rc == nil || rc.Session == nil {
		return errNotInAgentExec
	}
	delete(rc.Session.Values, key)
	return nil
}

// SendEvent 向当前 ChatModel 执行上下文发送 AgentEvent。
func SendEvent(ctx context.Context, event *AgentEvent) error {
	ec := getChatModelExecCtx(ctx)
	if ec == nil || ec.generator == nil {
		return errNotInAgentExec
	}
	ec.send(event)
	return nil
}

// TypedSendEvent 泛型版 SendEvent。
func TypedSendEvent[M MessageType](ctx context.Context, event *TypedAgentEvent[M]) error {
	ec := getReActExecCtx[M](ctx)
	if ec == nil || ec.generator == nil {
		return errNotInAgentExec
	}
	ec.send(event)
	return nil
}

type AgentExecError struct{ Message string }

func (e *AgentExecError) Error() string { return e.Message }

var errNotInAgentExec = &AgentExecError{Message: "must be called within ReActAgent Run/Resume"}

// checkGobEncodability 探测值能否作为 map[string]any 被 gob 编码
// （与会话值检查点序列化方式一致）
// 在 Set 时失败而非在 checkpoint/resume 时才报错
// letting them fail at checkpoint/resume time with a confusing error.
func checkGobEncodability(key string, value any) error {
	probe := map[string]any{key: value}
	if err := gob.NewEncoder(io.Discard).Encode(probe); err != nil {
		typeName := reflect.TypeOf(value).String()
		return &AgentExecError{Message: fmt.Sprintf(
			"SetRunLocalValue: the value (type %s) for key %q is not gob-serializable, "+
				"which means it will fail when the agent checkpoint is saved or resumed.\n\n"+
				"To fix this, register the type in an init() function in your package:\n\n"+
				"  func init() {\n"+
				"      schema.RegisterName[%s](\"a_unique_name_for_this_type\")\n"+
				"  }\n\n"+
				"This is required because agent state (including values set via SetRunLocalValue) is "+
				"persisted using gob encoding for interrupt/resume support. All concrete types stored "+
				"in interface-typed fields (like map[string]any) must be registered with gob.\n\n"+
				"If this value does not need to survive interrupt/resume, store it on the context instead, "+
				"for example via context.WithValue, so you don't need gob registration.\n\n"+
				"Underlying error: %v", typeName, key, typeName, err)}
	}
	return nil
}

// errNotInAgentExec 表示在 Run/Resume 外调用；SendEvent 需 generator 已就绪。
