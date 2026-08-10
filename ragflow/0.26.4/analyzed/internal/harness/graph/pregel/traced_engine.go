// traced_engine.go — TracedEngine：OpenTelemetry 追踪与生命周期回调包装层。

// Package pregel 为 Pregel Engine 提供追踪/回调包装，不修改 Engine 本体。
//
// TracedEngine 在 Run 外包裹 OTel span 与 RunStart/RunEnd 回调
// 回调管理独立于 Engine 结构体字段。
package pregel

import (
	"context"
	"fmt"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/codes"
	"go.opentelemetry.io/otel/trace"

	"ragflow/internal/harness/graph/constants"
	"ragflow/internal/harness/graph/types"
)

// tracedEngineTracerName OTel tracer 注册名。
const tracedEngineTracerName = "ragflow/internal/harness/graph/pregel/traced"

// TracedEngineOption 追踪行为配置函数。
type TracedEngineOption func(*tracedEngineConfig)

type tracedEngineConfig struct {
	enabled         bool
	recordArguments bool
	recordResults   bool
	eventFilter     func(string) bool
	callbacks       *CallbackManager
}

func defaultTracingConfig() *tracedEngineConfig {
	return &tracedEngineConfig{
		enabled:         true,
		recordArguments: true,
		recordResults:   true,
		eventFilter:     nil,
	}
}

// WithTracedEngineDisabled 禁用 OTel span 创建。
func WithTracedEngineDisabled() TracedEngineOption {
	return func(c *tracedEngineConfig) { c.enabled = false }
}

// WithTracedEngineRecordArgs 控制是否记录参数体量。
func WithTracedEngineRecordArgs(enabled bool) TracedEngineOption {
	return func(c *tracedEngineConfig) { c.recordArguments = enabled }
}

// WithTracedEngineRecordResults 控制是否记录结果体量。
func WithTracedEngineRecordResults(enabled bool) TracedEngineOption {
	return func(c *tracedEngineConfig) { c.recordResults = enabled }
}

// TracedEngine 包装 inner Engine，持有 tracer 与 CallbackManager。
// 回调通过 TracedEngine 单独管理，不写入 Engine。
type TracedEngine struct {
	inner     *Engine
	cfg       *tracedEngineConfig
	tracer    trace.Tracer
	callbacks *CallbackManager
}

// NewTracedEngine 创建追踪包装；禁用时仍可按配置分发回调。
// 追踪关闭时 Run/RunSync 仍可触发 WithEngineCallbacks 配置的回调
// 但不会创建 OTel span。
func NewTracedEngine(inner *Engine, opts ...TracedEngineOption) *TracedEngine {
	cfg := defaultTracingConfig()
	for _, opt := range opts {
		opt(cfg)
	}
	te := &TracedEngine{
		inner: inner,
		cfg:   cfg,
	}
	if cfg.enabled {
		te.tracer = otel.Tracer(tracedEngineTracerName)
	}
	if cfg.callbacks != nil {
		te.callbacks = cfg.callbacks
	}
	return te
}

// WithEngineCallbacks 设置 CallbackManager。
func WithEngineCallbacks(cb *CallbackManager) TracedEngineOption {
	return func(c *tracedEngineConfig) {
		c.callbacks = cb
	}
}

// SetCallbacks 运行时替换回调管理器。
func (te *TracedEngine) SetCallbacks(cb *CallbackManager) {
	te.callbacks = cb
}

// Run 带根 span、事件子 span 与回调执行图。
func (te *TracedEngine) Run(ctx context.Context, input any, mode types.StreamMode) (<-chan any, <-chan error) {
	if !te.cfg.enabled && te.callbacks == nil {
		return te.inner.Run(ctx, input, mode)
	}

	// Extract thread ID and graph name.
	threadID := extractThreadID(te.inner.config)
	graphName := "state_graph"
	if te.inner.graph != nil {
		nodes := te.inner.graph.GetNodes()
		if len(nodes) > 0 {
			for name := range nodes {
				graphName = "graph:" + name
				break
			}
		}
	}

	// Start root tracing span.
	var graphSpan trace.Span
	if te.tracer != nil {
		nodeCount := 0
		if te.inner.graph != nil {
			nodeCount = len(te.inner.graph.GetNodes())
		}
		attrs := []attribute.KeyValue{
			attribute.Int(AttrGraphNodes, nodeCount),
			attribute.Int(AttrRecursionLimit, te.inner.recursionLimit),
			attribute.String(AttrStreamMode, string(mode)),
		}
		if threadID != "" {
			attrs = append(attrs, attribute.String(AttrThreadID, threadID))
		}
		ctx, graphSpan = te.tracer.Start(ctx, SpanGraphRun,
			trace.WithSpanKind(trace.SpanKindInternal),
			trace.WithAttributes(attrs...),
		)
	}

	// Dispatch OnRunStart.
	if te.callbacks != nil {
		te.callbacks.RunStart(ctx, graphName, threadID)
	}

	// Execute the inner engine.
	outputCh, errCh := te.inner.Run(ctx, input, mode)

	// Wrap outputCh with tracing.
	if te.tracer == nil {
		return outputCh, wrapErrChWithCallback(errCh, te, graphName, threadID, graphSpan)
	}

	tracedOutputCh := make(chan any, 100)
	go func() {
		defer close(tracedOutputCh)
		for event := range outputCh {
			te.traceEvent(ctx, event, graphSpan)
			tracedOutputCh <- event
		}
	}()

	return tracedOutputCh, wrapErrChWithCallback(errCh, te, graphName, threadID, graphSpan)
}

// RunSync 同步模式，抽取 Final 事件中的 state。
func (te *TracedEngine) RunSync(ctx context.Context, input any) (any, error) {
	outputCh, errCh := te.Run(ctx, input, types.StreamModeValues)
	// Drain outputCh.
	var finalState any
	for result := range outputCh {
		if se, ok := result.(*StreamEvent); ok && se.Type == EventTypeFinal {
			if data, ok := se.Data.(map[string]any); ok {
				if state, ok := data["state"]; ok {
					finalState = state
				}
			}
		}
	}
	err := <-errCh
	return finalState, err
}

// ---- 辅助函数 ----

// extractThreadID 从 RunnableConfig.Configurable 提取 thread_id。
func extractThreadID(cfg *types.RunnableConfig) string {
	if cfg == nil || cfg.Configurable == nil {
		return ""
	}
	if tid, _ := cfg.Configurable[constants.ConfigKeyThreadID].(string); tid != "" {
		return tid
	}
	return ""
}

// traceEvent 为 checkpoint/interrupt/error/task 事件创建子 span。
func (te *TracedEngine) traceEvent(ctx context.Context, event any, rootSpan trace.Span) {
	if te.tracer == nil || rootSpan == nil {
		return
	}
	se, ok := event.(*StreamEvent)
	if !ok {
		return
	}
	switch se.Type {
	case EventTypeCheckpoint:
		// Checkpoint events under root span.
		_, cpSpan := te.tracer.Start(ctx, SpanCheckpoint,
			trace.WithSpanKind(trace.SpanKindInternal),
			trace.WithAttributes(
				attribute.Int(AttrStepNum, se.Step),
				attribute.String(AttrNodeName, se.Node),
			),
		)
		cpSpan.SetStatus(codes.Ok, "")
		cpSpan.End()
	case EventTypeInterrupt:
		_, intSpan := te.tracer.Start(ctx, SpanInterrupt,
			trace.WithSpanKind(trace.SpanKindInternal),
			trace.WithAttributes(
				attribute.Int(AttrStepNum, se.Step),
				attribute.String(AttrInterruptNode, se.Node),
			),
		)
		intSpan.SetStatus(codes.Ok, "")
		intSpan.End()
	case EventTypeError:
		if rootSpan != nil {
			rootSpan.SetStatus(codes.Error, fmt.Sprintf("%v", se.Error))
			rootSpan.RecordError(se.Error)
		}
	case EventTypeTaskStart:
		_, taskSpan := te.tracer.Start(ctx, SpanNodeExecute,
			trace.WithSpanKind(trace.SpanKindInternal),
			trace.WithAttributes(
				attribute.Int(AttrStepNum, se.Step),
				attribute.String(AttrNodeName, se.Node),
			),
		)
		taskSpan.SetStatus(codes.Ok, "")
		taskSpan.End()
	}
}

// wrapErrChWithCallback 在错误通道上触发 RunEnd 并结束根 span。
func wrapErrChWithCallback(errCh <-chan error, te *TracedEngine, graphName, threadID string, graphSpan trace.Span) <-chan error {
	if te.callbacks == nil && (te.tracer == nil || graphSpan == nil) {
		return errCh
	}
	wrapped := make(chan error, 1)
	go func() {
		defer close(wrapped)
		err, ok := <-errCh
		// Dispatch callbacks.
		if te.callbacks != nil {
			te.callbacks.RunEnd(context.Background(), graphName, threadID, err)
		}
		// End root span.
		if graphSpan != nil {
			if err != nil {
				graphSpan.SetStatus(codes.Error, err.Error())
				graphSpan.RecordError(err)
			} else {
				graphSpan.SetStatus(codes.Ok, "")
			}
			graphSpan.End()
		}
		if ok {
			wrapped <- err
		}
	}()
	return wrapped
}

// 流式输出经 tracedOutputCh 转发，每个 StreamEvent 可附加独立子 span。
