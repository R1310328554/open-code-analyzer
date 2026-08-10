// telemetry.go — OpenTelemetry 追踪中间件：为模型调用与工具执行创建 span。

// Package telemetry 为 harness-go ReAct Agent 提供 OpenTelemetry 追踪中间件。
//
// Usage:
//
//	import telemetrymw "ragflow/internal/harness/core/middlewares/telemetry"
//
//	cfg := core.DefaultReActConfig[*schema.Message]()
//	cfg.Middlewares = append(cfg.Middlewares, telemetrymw.New())
//
// To customize:
//
//	mw := telemetrymw.New(telemetrymw.WithTracing(false))
//
// 使用 RAGFlow 全局 TracerProvider（见 internal/observability/otel）。
// 仅当 OTLP 端点已配置时 tracing 生效。
// has been initialized with an OTLP collector endpoint.
package telemetry

import (
	"context"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/codes"
	"go.opentelemetry.io/otel/trace"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// Config 配置 telemetry 中间件。
type Config struct {
	EnableTracing bool
}

// Option 函数式配置选项。
type Option func(*Config)

// WithTracing 开关分布式追踪。
func WithTracing(enabled bool) Option {
	return func(c *Config) { c.EnableTracing = enabled }
}

func defaultConfig() *Config {
	return &Config{EnableTracing: true}
}

const tracerName = "ragflow/internal/harness/core/middlewares/telemetry"

// Middleware 用 OpenTelemetry span 包装 Agent 执行路径。
// OpenTelemetry tracing spans. It wraps model calls and tool invocations.
//
// 注意：当前仅 TracerProvider，Metrics 待 MeterProvider 接入后恢复。
// a TracerProvider (see internal/observability/otel). Once a MeterProvider
// is added, metrics recording can be restored here.
//
// TODO：泛型化以同时支持 AgenticMessage 与 *schema.Message。
// *schema.Message. Currently hardcoded to *schema.Message, unlike other
// middlewares that use BaseMiddleware[M].
type Middleware struct {
	core.BaseMiddleware[*schema.Message]
	cfg    *Config
	tracer trace.Tracer
}

// New 创建 telemetry 中间件，默认启用 tracing。
func New(opts ...Option) *Middleware {
	cfg := defaultConfig()
	for _, opt := range opts {
		opt(cfg)
	}
	m := &Middleware{cfg: cfg}
	if cfg.EnableTracing {
		m.tracer = otel.Tracer(tracerName)
	}
	return m
}

// recordSpanError 将 span 标为 Error 并记录 err。
func recordSpanError(span trace.Span, err error) {
	if span == nil || err == nil {
		return
	}
	span.SetStatus(codes.Error, err.Error())
	span.RecordError(err)
}

// WrapModel 包装 Model 为 tracedModel，Generate/Stream 各建 span。
func (m *Middleware) WrapModel(ctx context.Context, model core.Model[*schema.Message], mc *core.ModelContext) (core.Model[*schema.Message], error) {
	if m.tracer == nil {
		return model, nil
	}
	return &tracedModel{
		inner:   model,
		mw:      m,
		toolCnt: len(mc.Tools),
	}, nil
}

// WrapToolInvoke 为同步工具调用创建 internal span。
func (m *Middleware) WrapToolInvoke(ctx context.Context, ep core.InvokableToolEndpoint, tc *core.ToolContext) (core.InvokableToolEndpoint, error) {
	if m.tracer == nil {
		return ep, nil
	}
	return func(ctx context.Context, args string, opts ...core.ToolOption) (string, error) {
		var span trace.Span
		if m.cfg.EnableTracing {
			ctx, span = m.tracer.Start(ctx, "tool."+tc.Name,
				trace.WithAttributes(
					attribute.String("tool.name", tc.Name),
					attribute.Int("args.size", len(args)),
				),
				trace.WithSpanKind(trace.SpanKindInternal),
			)
		}
		result, err := ep(ctx, args, opts...)
		if span != nil && span.IsRecording() {
			if err != nil {
				recordSpanError(span, err)
			} else {
				span.SetStatus(codes.Ok, "")
			}
			span.End()
		}
		return result, err
	}, nil
}

// WrapToolStream 为流式工具调用创建 span。
func (m *Middleware) WrapToolStream(ctx context.Context, ep core.StreamableToolEndpoint, tc *core.ToolContext) (core.StreamableToolEndpoint, error) {
	if m.tracer == nil {
		return ep, nil
	}
	return func(ctx context.Context, args string, opts ...core.ToolOption) (*schema.StreamReader[string], error) {
		var span trace.Span
		if m.cfg.EnableTracing {
			ctx, span = m.tracer.Start(ctx, "tool.stream."+tc.Name,
				trace.WithAttributes(attribute.String("tool.name", tc.Name)),
				trace.WithSpanKind(trace.SpanKindInternal),
			)
		}
		result, err := ep(ctx, args, opts...)
		if err != nil {
			if span != nil {
				recordSpanError(span, err)
				span.End()
			}
			return nil, err
		}
		if span != nil {
			span.SetStatus(codes.Ok, "")
			span.End()
		}
		return result, nil
	}, nil
}

// WrapEnhancedInvokableToolCall 包装增强型同步工具调用。
func (m *Middleware) WrapEnhancedInvokableToolCall(ctx context.Context, ep core.EnhancedInvokableToolEndpoint, tc *core.ToolContext) (core.EnhancedInvokableToolEndpoint, error) {
	if m.tracer == nil {
		return ep, nil
	}
	return func(ctx context.Context, args *schema.ToolArgument, opts ...core.ToolOption) (*schema.ToolResult, error) {
		var span trace.Span
		if m.cfg.EnableTracing {
			ctx, span = m.tracer.Start(ctx, "enhanced_tool."+tc.Name,
				trace.WithAttributes(attribute.String("tool.name", tc.Name)),
				trace.WithSpanKind(trace.SpanKindInternal),
			)
		}
		result, err := ep(ctx, args, opts...)
		if span != nil && span.IsRecording() {
			if err != nil {
				recordSpanError(span, err)
			} else {
				span.SetStatus(codes.Ok, "")
			}
			span.End()
		}
		return result, err
	}, nil
}

// WrapEnhancedStreamableToolCall 包装增强型流式工具调用。
func (m *Middleware) WrapEnhancedStreamableToolCall(ctx context.Context, ep core.EnhancedStreamableToolEndpoint, tc *core.ToolContext) (core.EnhancedStreamableToolEndpoint, error) {
	if m.tracer == nil {
		return ep, nil
	}
	return func(ctx context.Context, args *schema.ToolArgument, opts ...core.ToolOption) (*schema.StreamReader[*schema.ToolResult], error) {
		var span trace.Span
		if m.cfg.EnableTracing {
			ctx, span = m.tracer.Start(ctx, "enhanced_tool.stream."+tc.Name,
				trace.WithAttributes(attribute.String("tool.name", tc.Name)),
				trace.WithSpanKind(trace.SpanKindInternal),
			)
		}
		result, err := ep(ctx, args, opts...)
		if err != nil {
			if span != nil {
				recordSpanError(span, err)
				span.End()
			}
			return nil, err
		}
		if span != nil {
			span.SetStatus(codes.Ok, "")
			span.End()
		}
		return result, nil
	}, nil
}

// tracedModel 在 Generate/Stream 边界记录 messages/tools 属性。
type tracedModel struct {
	inner   core.Model[*schema.Message]
	mw      *Middleware
	toolCnt int
}

func (m *tracedModel) Generate(ctx context.Context, msgs []*schema.Message, opts ...core.ModelOption) (*schema.Message, error) {
	var span trace.Span
	if m.mw.cfg.EnableTracing && m.mw.tracer != nil {
		ctx, span = m.mw.tracer.Start(ctx, "model.generate",
			trace.WithAttributes(
				attribute.Int("messages.count", len(msgs)),
				attribute.Int("tools.count", m.toolCnt),
			),
			trace.WithSpanKind(trace.SpanKindClient),
		)
	}
	resp, err := m.inner.Generate(ctx, msgs, opts...)
	if span != nil && span.IsRecording() {
		if err != nil {
			recordSpanError(span, err)
		} else {
			span.SetStatus(codes.Ok, "")
		}
		span.End()
	}
	return resp, err
}

func (m *tracedModel) Stream(ctx context.Context, msgs []*schema.Message, opts ...core.ModelOption) (*schema.StreamReader[*schema.Message], error) {
	var span trace.Span
	if m.mw.cfg.EnableTracing && m.mw.tracer != nil {
		ctx, span = m.mw.tracer.Start(ctx, "model.stream",
			trace.WithAttributes(
				attribute.Int("messages.count", len(msgs)),
				attribute.Int("tools.count", m.toolCnt),
			),
			trace.WithSpanKind(trace.SpanKindClient),
		)
	}
	result, err := m.inner.Stream(ctx, msgs, opts...)
	if err != nil {
		if span != nil {
			recordSpanError(span, err)
			span.End()
		}
		return nil, err
	}
	if span != nil {
		span.SetStatus(codes.Ok, "")
		span.End()
	}
	return result, nil
}

func (m *tracedModel) BindTools(tools []*schema.ToolInfo) error {
	return m.inner.BindTools(tools)
}

// 编译期断言 Middleware 实现 ReActMiddleware。
var _ core.ReActMiddleware = (*Middleware)(nil)

// tracer 为 nil 或 EnableTracing 关闭时各 Wrap* 直接透传原始 endpoint。
