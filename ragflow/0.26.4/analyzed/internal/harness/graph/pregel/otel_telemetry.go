// Package pregel 为 Pregel 图执行提供 OpenTelemetry 分布式追踪。
//
// 在图运行、超步、节点执行、检查点与中断等关键路径创建 Span。
package pregel

import (
	"context"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/codes"
	"go.opentelemetry.io/otel/trace"
)

const tracerName = "ragflow/internal/harness/graph/pregel"

// Tracer Pregel 引擎的全局 OpenTelemetry Tracer。
var tracer trace.Tracer

func init() {
	tracer = otel.Tracer(tracerName)
}

// SpanAttr Pregel 引擎 Span 属性键名。
const (
	AttrStepNum        = "pregel.step"
	AttrGraphName      = "pregel.graph.name"
	AttrGraphNodes     = "pregel.graph.nodes"
	AttrGraphEdges     = "pregel.graph.edges"
	AttrNodeName       = "pregel.node.name"
	AttrNodeTrigger    = "pregel.node.trigger"
	AttrTaskCount      = "pregel.task.count"
	AttrChannelCount   = "pregel.channel.count"
	AttrThreadID       = "pregel.thread_id"
	AttrCheckpointID   = "pregel.checkpoint_id"
	AttrRecursionLimit = "pregel.recursion_limit"
	AttrInterruptNode  = "pregel.interrupt.node"
	AttrDurability     = "pregel.durability"
	AttrStreamMode     = "pregel.stream_mode"
	AttrStateKeys      = "pregel.state.keys"
	AttrInputSize      = "pregel.input.size"
	AttrOutputSize     = "pregel.output.size"
	AttrErrorCode      = "pregel.error.code"
	AttrCacheHit       = "pregel.cache.hit"
	AttrTaskDuration   = "pregel.task.duration_ms"
)

// Span 名称常量。
const (
	SpanGraphRun      = "pregel.Run"
	SpanGraphStep     = "pregel.Superstep"
	SpanNodeExecute   = "pregel.Node.Exec"
	SpanPrepareTasks  = "pregel.PrepareTasks"
	SpanApplyWrites   = "pregel.ApplyWrites"
	SpanCheckpoint    = "pregel.Checkpoint"
	SpanInterrupt     = "pregel.Interrupt"
	SpanResume        = "pregel.Resume"
	SpanBuildOutput   = "pregel.BuildOutput"
	SpanSearchChannel = "pregel.SearchChannel"
)

// TraceOption 追踪配置函数选项。
type TraceOption func(*traceConfig)

type traceConfig struct {
	enabled         bool
	attrFilter      func(key, value string) bool // return true to include
	recordArguments bool
	recordResults   bool
}

func defaultTraceConfig() *traceConfig {
	return &traceConfig{
		enabled:         true,
		recordArguments: true,
		recordResults:   true,
		attrFilter:      nil,
	}
}

// WithTraceDisabled 禁用追踪。
func WithTraceDisabled() TraceOption {
	return func(c *traceConfig) { c.enabled = false }
}

// WithTraceNoArgs 不记录参数大小。
func WithTraceNoArgs() TraceOption {
	return func(c *traceConfig) { c.recordArguments = false }
}

// WithTraceNoResults 不记录结果大小。
func WithTraceNoResults() TraceOption {
	return func(c *traceConfig) { c.recordResults = false }
}

// WithTraceAttrFilter 设置属性过滤函数。
func WithTraceAttrFilter(fn func(key, value string) bool) TraceOption {
	return func(c *traceConfig) { c.attrFilter = fn }
}

// startGraphSpan 创建图运行的根 Span。
// It returns the span and context with the span attached.
func startGraphSpan(ctx context.Context, graphName string, nodeCount, edgeCount, recLimit int, threadID string, durability, streamMode string) (context.Context, trace.Span) {
	if tracer == nil {
		return ctx, trace.SpanFromContext(ctx)
	}
	opts := []trace.SpanStartOption{
		trace.WithSpanKind(trace.SpanKindInternal),
		trace.WithAttributes(
			attribute.String(AttrGraphName, graphName),
			attribute.Int(AttrGraphNodes, nodeCount),
			attribute.Int(AttrGraphEdges, edgeCount),
			attribute.Int(AttrRecursionLimit, recLimit),
			attribute.String(AttrDurability, durability),
			attribute.String(AttrStreamMode, streamMode),
		),
	}
	if threadID != "" {
		opts = append(opts, trace.WithAttributes(attribute.String(AttrThreadID, threadID)))
	}
	ctx, span := tracer.Start(ctx, SpanGraphRun, opts...)
	return ctx, span
}

// endGraphSpan 结束根 Span 并设置状态。
func endGraphSpan(span trace.Span, err error) {
	if span == nil || !span.IsRecording() {
		return
	}
	if err != nil {
		span.SetStatus(codes.Error, err.Error())
		span.RecordError(err)
	} else {
		span.SetStatus(codes.Ok, "")
	}
	span.End()
}

// startStepSpan 创建超步 Span。
func startStepSpan(ctx context.Context, step int, taskCount int) (context.Context, trace.Span) {
	if tracer == nil {
		return ctx, trace.SpanFromContext(ctx)
	}
	ctx, span := tracer.Start(ctx, SpanGraphStep,
		trace.WithSpanKind(trace.SpanKindInternal),
		trace.WithAttributes(
			attribute.Int(AttrStepNum, step),
			attribute.Int(AttrTaskCount, taskCount),
		),
	)
	return ctx, span
}

// endStepSpan 结束超步 Span。
func endStepSpan(span trace.Span, err error) {
	if span == nil || !span.IsRecording() {
		return
	}
	if err != nil {
		span.SetStatus(codes.Error, err.Error())
		span.RecordError(err)
	} else {
		span.SetStatus(codes.Ok, "")
	}
	span.End()
}

// startNodeSpan 创建节点执行 Span。
func startNodeSpan(ctx context.Context, nodeName string, triggerCount int, inputSize int) (context.Context, trace.Span) {
	if tracer == nil {
		return ctx, trace.SpanFromContext(ctx)
	}
	ctx, span := tracer.Start(ctx, SpanNodeExecute,
		trace.WithSpanKind(trace.SpanKindInternal),
		trace.WithAttributes(
			attribute.String(AttrNodeName, nodeName),
			attribute.Int("pregel.node.trigger_count", triggerCount),
			attribute.Int(AttrInputSize, inputSize),
		),
	)
	return ctx, span
}

// endNodeSpan 结束节点 Span 并记录输出大小。
func endNodeSpan(span trace.Span, outputSize int, err error) {
	if span == nil || !span.IsRecording() {
		return
	}
	span.SetAttributes(attribute.Int(AttrOutputSize, outputSize))
	if err != nil {
		span.SetStatus(codes.Error, err.Error())
		span.RecordError(err)
	} else {
		span.SetStatus(codes.Ok, "")
	}
	span.End()
}

// startCheckpointSpan 创建检查点操作 Span。
func startCheckpointSpan(ctx context.Context, operation string, threadID, checkpointID string, stateSize int) (context.Context, trace.Span) {
	if tracer == nil {
		return ctx, trace.SpanFromContext(ctx)
	}
	ctx, span := tracer.Start(ctx, SpanCheckpoint,
		trace.WithSpanKind(trace.SpanKindInternal),
		trace.WithAttributes(
			attribute.String("pregel.checkpoint.operation", operation),
			attribute.Int(AttrStateKeys, stateSize),
		),
	)
	if threadID != "" {
		span.SetAttributes(attribute.String(AttrThreadID, threadID))
	}
	if checkpointID != "" {
		span.SetAttributes(attribute.String(AttrCheckpointID, checkpointID))
	}
	return ctx, span
}

// endCheckpointSpan 结束检查点 Span。
func endCheckpointSpan(span trace.Span, err error) {
	endSpan(span, err)
}

// endSpan 通用 Span 结束（含错误状态）。
func endSpan(span trace.Span, err error) {
	if span == nil || !span.IsRecording() {
		return
	}
	if err != nil {
		span.SetStatus(codes.Error, err.Error())
		span.RecordError(err)
	} else {
		span.SetStatus(codes.Ok, "")
	}
	span.End()
}

// startInterruptSpan 创建中断 Span。
func startInterruptSpan(ctx context.Context, nodeNames []string) (context.Context, trace.Span) {
	if tracer == nil {
		return ctx, trace.SpanFromContext(ctx)
	}
	var names []attribute.KeyValue
	for _, n := range nodeNames {
		names = append(names, attribute.String(AttrInterruptNode, n))
	}
	ctx, span := tracer.Start(ctx, SpanInterrupt,
		trace.WithSpanKind(trace.SpanKindInternal),
		trace.WithAttributes(names...),
	)
	return ctx, span
}

// startPrepareTasksSpan 创建任务准备 Span。
func startPrepareTasksSpan(ctx context.Context, completedCount int) (context.Context, trace.Span) {
	if tracer == nil {
		return ctx, trace.SpanFromContext(ctx)
	}
	ctx, span := tracer.Start(ctx, SpanPrepareTasks,
		trace.WithSpanKind(trace.SpanKindInternal),
		trace.WithAttributes(attribute.Int("pregel.completed_tasks", completedCount)),
	)
	return ctx, span
}

// endPrepareTasksSpan 结束任务准备 Span。
func endPrepareTasksSpan(span trace.Span, taskCount int) {
	if span == nil || !span.IsRecording() {
		return
	}
	span.SetAttributes(attribute.Int(AttrTaskCount, taskCount))
	span.SetStatus(codes.Ok, "")
	span.End()
}

// startApplyWritesSpan 创建写入应用 Span。
func startApplyWritesSpan(ctx context.Context, resultCount int) (context.Context, trace.Span) {
	if tracer == nil {
		return ctx, trace.SpanFromContext(ctx)
	}
	ctx, span := tracer.Start(ctx, SpanApplyWrites,
		trace.WithSpanKind(trace.SpanKindInternal),
		trace.WithAttributes(attribute.Int("pregel.results", resultCount)),
	)
	return ctx, span
}

// endApplyWritesSpan 结束写入应用 Span。
func endApplyWritesSpan(span trace.Span, err error) {
	endSpan(span, err)
}
