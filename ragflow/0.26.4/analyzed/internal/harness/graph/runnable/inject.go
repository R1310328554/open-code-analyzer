// inject.go — Runnable 依赖注入：InjectionContext、参数反射与追踪包装。

// Package runnable 为 Runnable 提供依赖注入与追踪支持。
package runnable

import (
	"context"
	"fmt"
	"reflect"
	"sync"

	"ragflow/internal/harness/graph/types"
)

// InjectionContext 依赖注入上下文（Config、Previous、Runtime、Callbacks）。
type InjectionContext struct {
	// Config 当前执行的 RunnableConfig。
	Config *types.RunnableConfig

	// Previous 上次执行结果，供 checkpointer 场景注入。
	Previous interface{}

	// Runtime 运行时键值（store、writer 等）。
	Runtime map[string]interface{}

	// TracingEnabled 是否启用追踪。
	TracingEnabled bool

	// Callbacks 执行生命周期回调列表。
	Callbacks []Callback
}

// Callback 执行回调接口（Start/End/Error）。
type Callback interface {
	// OnStart 执行开始时调用。
	OnStart(ctx context.Context, name string, input interface{})
	// OnEnd 执行结束时调用。
	OnEnd(ctx context.Context, name string, output interface{}, err error)
	// OnError 发生错误时调用。
	OnError(ctx context.Context, name string, err error)
}

// CallbackFunc 函数式 Callback 实现。
type CallbackFunc struct {
	OnStartFunc func(ctx context.Context, name string, input interface{})
	OnEndFunc   func(ctx context.Context, name string, output interface{}, err error)
	OnErrorFunc func(ctx context.Context, name string, err error)
}

// OnStart implements Callback.
func (c *CallbackFunc) OnStart(ctx context.Context, name string, input interface{}) {
	if c.OnStartFunc != nil {
		c.OnStartFunc(ctx, name, input)
	}
}

// OnEnd implements Callback.
func (c *CallbackFunc) OnEnd(ctx context.Context, name string, output interface{}, err error) {
	if c.OnEndFunc != nil {
		c.OnEndFunc(ctx, name, output, err)
	}
}

// OnError implements Callback.
func (c *CallbackFunc) OnError(ctx context.Context, name string, err error) {
	if c.OnErrorFunc != nil {
		c.OnErrorFunc(ctx, name, err)
	}
}

// Injectable 支持注入上下文读写的接口。
type Injectable interface {
	// SetInjectionContext 设置注入上下文。
	SetInjectionContext(ctx *InjectionContext)
	// GetInjectionContext 返回当前注入上下文。
	GetInjectionContext() *InjectionContext
}

// InjectableRunnable 带 ParamInjector 的可注入 Runnable。
type InjectableRunnable struct {
	name          string
	fn            interface{}
	injectionCtx  *InjectionContext
	paramInjector *ParamInjector
	schema        *RunnableSchema
}

// NewInjectableRunnable 创建可注入 Runnable。
func NewInjectableRunnable(name string, fn interface{}) *InjectableRunnable {
	return &InjectableRunnable{
		name:          name,
		fn:            fn,
		paramInjector: NewParamInjector(),
		schema: &RunnableSchema{
			Name: name,
		},
	}
}

// SetInjectionContext sets the injection context.
func (r *InjectableRunnable) SetInjectionContext(ctx *InjectionContext) {
	r.injectionCtx = ctx
}

// GetInjectionContext returns the injection context.
func (r *InjectableRunnable) GetInjectionContext() *InjectionContext {
	return r.injectionCtx
}

// WithConfigInjector 注册 config 参数注入器。
func (r *InjectableRunnable) WithConfigInjector() *InjectableRunnable {
	r.paramInjector.AddInjector("config", func(ctx *InjectionContext) interface{} {
		return ctx.Config
	})
	return r
}

// WithPreviousInjector 注册 previous 结果注入器。
func (r *InjectableRunnable) WithPreviousInjector() *InjectableRunnable {
	r.paramInjector.AddInjector("previous", func(ctx *InjectionContext) interface{} {
		return ctx.Previous
	})
	return r
}

// WithRuntimeInjector 从 Runtime map 按 key 注入。
func (r *InjectableRunnable) WithRuntimeInjector(key string) *InjectableRunnable {
	r.paramInjector.AddInjector(key, func(ctx *InjectionContext) interface{} {
		if ctx.Runtime != nil {
			return ctx.Runtime[key]
		}
		return nil
	})
	return r
}

// WithCallback 追加执行回调。
func (r *InjectableRunnable) WithCallback(cb Callback) *InjectableRunnable {
	if r.injectionCtx == nil {
		r.injectionCtx = &InjectionContext{}
	}
	r.injectionCtx.Callbacks = append(r.injectionCtx.Callbacks, cb)
	return r
}

// Invoke 构建反射参数、执行函数并分发回调。
func (r *InjectableRunnable) Invoke(ctx context.Context, input interface{}) (interface{}, error) {
	// Notify callbacks
	if r.injectionCtx != nil {
		for _, cb := range r.injectionCtx.Callbacks {
			cb.OnStart(ctx, r.name, input)
		}
	}

	// Build arguments with injection
	args, err := r.buildArguments(ctx, input)
	if err != nil {
		if r.injectionCtx != nil {
			for _, cb := range r.injectionCtx.Callbacks {
				cb.OnError(ctx, r.name, err)
				cb.OnEnd(ctx, r.name, nil, err)
			}
		}
		return nil, err
	}

	// Execute function
	output, err := r.executeWithArgs(args)

	// Notify callbacks
	if r.injectionCtx != nil {
		for _, cb := range r.injectionCtx.Callbacks {
			if err != nil {
				cb.OnError(ctx, r.name, err)
			}
			cb.OnEnd(ctx, r.name, output, err)
		}
	}

	return output, err
}

// buildArguments 反射解析函数签名并填充注入参数。
func (r *InjectableRunnable) buildArguments(ctx context.Context, input interface{}) ([]reflect.Value, error) {
	fnValue := reflect.ValueOf(r.fn)
	fnType := fnValue.Type()

	if fnType.Kind() != reflect.Func {
		return nil, fmt.Errorf("runnable must be a function")
	}

	numParams := fnType.NumIn()
	args := make([]reflect.Value, numParams)

	argIndex := 0

	// First argument is usually context
	if numParams > 0 {
		firstParamType := fnType.In(0)
		if firstParamType.Implements(reflect.TypeOf((*context.Context)(nil)).Elem()) {
			args[0] = reflect.ValueOf(ctx)
			argIndex++
		}
	}

	// Inject configured dependencies
	for argIndex < numParams {
		paramType := fnType.In(argIndex)
		injected := false

		// Try to inject from injection context
		if r.injectionCtx != nil {
			// Check for config injection
			if paramType == reflect.TypeOf(&types.RunnableConfig{}) {
				args[argIndex] = reflect.ValueOf(r.injectionCtx.Config)
				injected = true
			}

			// Check runtime values
			if !injected && r.injectionCtx.Runtime != nil {
				for _, value := range r.injectionCtx.Runtime {
					if value != nil && reflect.TypeOf(value).AssignableTo(paramType) {
						args[argIndex] = reflect.ValueOf(value)
						injected = true
						break
					}
				}
			}
		}

		// If not injected, use input
		if !injected {
			if input != nil && reflect.TypeOf(input).AssignableTo(paramType) {
				args[argIndex] = reflect.ValueOf(input)
			} else {
				// Try to convert
				inputValue := reflect.ValueOf(input)
				if inputValue.Type().ConvertibleTo(paramType) {
					args[argIndex] = inputValue.Convert(paramType)
				} else {
					// Zero value
					args[argIndex] = reflect.Zero(paramType)
				}
			}
		}

		argIndex++
	}

	return args, nil
}

// executeWithArgs executes the function with the given arguments.
func (r *InjectableRunnable) executeWithArgs(args []reflect.Value) (interface{}, error) {
	fnValue := reflect.ValueOf(r.fn)
	results := fnValue.Call(args)

	// Handle return values
	if len(results) == 0 {
		return nil, nil
	}

	if len(results) == 1 {
		// Single return value (output or error)
		if results[0].Type().Implements(reflect.TypeOf((*error)(nil)).Elem()) {
			if !results[0].IsNil() {
				if err, ok := results[0].Interface().(error); ok {
					return nil, err
				}
				return nil, fmt.Errorf("expected error, got %T", results[0].Interface())
			}
			return nil, nil
		}
		return results[0].Interface(), nil
	}

	// Two return values (output, error)
	output := results[0].Interface()
	errVal := results[1]
	// IsNil panics on non-nillable types (int, string, struct, etc.).
	// Guard with a kind check before calling IsNil.
	if errVal.Kind() == reflect.Interface || errVal.Kind() == reflect.Ptr {
		if !errVal.IsNil() {
			if err, ok := results[1].Interface().(error); ok {
				return output, err
			}
			return output, fmt.Errorf("expected error, got %T", results[1].Interface())
		}
	}
	return output, nil
}

// GetSchema returns the schema.
func (r *InjectableRunnable) GetSchema() *RunnableSchema {
	return r.schema
}

// ParamInjector handles parameter injection.
type ParamInjector struct {
	injectors map[string]func(*InjectionContext) interface{}
}

// NewParamInjector creates a new parameter injector.
func NewParamInjector() *ParamInjector {
	return &ParamInjector{
		injectors: make(map[string]func(*InjectionContext) interface{}),
	}
}

// AddInjector adds an injector for a parameter name.
func (p *ParamInjector) AddInjector(name string, injector func(*InjectionContext) interface{}) {
	p.injectors[name] = injector
}

// Inject injects parameters into the context.
func (p *ParamInjector) Inject(ctx *InjectionContext, name string) interface{} {
	if injector, ok := p.injectors[name]; ok {
		return injector(ctx)
	}
	return nil
}

// Tracer provides execution tracing support.
type Tracer struct {
	mu    sync.Mutex
	spans []Span
}

// Span represents a trace span.
type Span struct {
	Name      string
	Input     interface{}
	Output    interface{}
	Error     error
	StartTime int64
	EndTime   int64
}

// NewTracer creates a new tracer.
func NewTracer() *Tracer {
	return &Tracer{
		spans: make([]Span, 0),
	}
}

// TraceCallback returns a callback that records traces.
// The returned Callback is safe for concurrent use.
func (t *Tracer) TraceCallback() Callback {
	var mu sync.Mutex
	var currentInput interface{}
	return &CallbackFunc{
		OnStartFunc: func(ctx context.Context, name string, input interface{}) {
			mu.Lock()
			currentInput = input
			mu.Unlock()
		},
		OnEndFunc: func(ctx context.Context, name string, output interface{}, err error) {
			mu.Lock()
			inp := currentInput
			mu.Unlock()
			t.mu.Lock()
			t.spans = append(t.spans, Span{
				Name:   name,
				Input:  inp,
				Output: output,
				Error:  err,
			})
			t.mu.Unlock()
		},
	}
}

// GetSpans returns all recorded spans.
func (t *Tracer) GetSpans() []Span {
	t.mu.Lock()
	defer t.mu.Unlock()
	result := make([]Span, len(t.spans))
	copy(result, t.spans)
	return result
}

// Clear clears all spans.
func (t *Tracer) Clear() {
	t.mu.Lock()
	defer t.mu.Unlock()
	t.spans = make([]Span, 0)
}

// CoerceToRunnable coerces various types to an InjectableRunnable.
// Supports:
// - Functions: wrapped with NewInjectableRunnable
// - Runnable: wrapped to support injection
// - InjectableRunnable: returned as-is
func CoerceToInjectableRunnable(target interface{}, name string) (*InjectableRunnable, error) {
	switch r := target.(type) {
	case *InjectableRunnable:
		return r, nil
	case Runnable[any, any]:
		// Wrap existing runnable
		return NewInjectableRunnable(name, func(ctx context.Context, input interface{}) (interface{}, error) {
			return r.Invoke(ctx, input)
		}), nil
	default:
		// Check if it's a function
		targetType := reflect.TypeOf(target)
		if targetType == nil {
			return nil, fmt.Errorf("cannot coerce nil to InjectableRunnable")
		}
		if targetType.Kind() == reflect.Func {
			return NewInjectableRunnable(name, target), nil
		}
		return nil, fmt.Errorf("cannot coerce %T to InjectableRunnable", target)
	}
}

// WithTracing enables tracing for a runnable.
func WithTracing(r *InjectableRunnable, tracer *Tracer) *InjectableRunnable {
	return r.WithCallback(tracer.TraceCallback())
}

// InjectDependencies creates an injection context and injects it into the runnable.
func InjectDependencies(r *InjectableRunnable, config *types.RunnableConfig, previous interface{}, runtime map[string]interface{}) *InjectableRunnable {
	ctx := &InjectionContext{
		Config:   config,
		Previous: previous,
		Runtime:  runtime,
	}
	r.SetInjectionContext(ctx)
	return r
}

// ParamInjector 按参数名匹配注入器；首参通常为 context.Context。
