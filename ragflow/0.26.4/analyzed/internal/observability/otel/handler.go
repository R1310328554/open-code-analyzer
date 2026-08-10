/*
 * Copyright 2026 The RAGFlow Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package otel

import (
	"context"
	"sync"
	"time"

	"github.com/cloudwego/eino/callbacks"
	"github.com/cloudwego/eino/schema"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/codes"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	"go.opentelemetry.io/otel/trace"
	"go.opentelemetry.io/otel/trace/embedded"
	"go.opentelemetry.io/otel/trace/noop"
)

// TracerName 为 OtelHandler 创建的全部 span 使用的 instrumentation scope。
const TracerName = "github.com/infiniflow/ragflow/internal/observability/otel"

// Context-key 类型；未导出以防外部包与回调 context 键冲突。
type (
	runIDKeyType       struct{}
	sessionIDKeyType   struct{}
	spanContextKeyType struct{}
)

// WithRunID 在 ctx 上携带 canvas run id，Handler 将其附加为 run.id 属性。
func WithRunID(ctx context.Context, runID string) context.Context {
	return context.WithValue(ctx, runIDKeyType{}, runID)
}

// RunIDFromContext 返回 ctx 上的 run id，无则返回空串。
func RunIDFromContext(ctx context.Context) string {
	if v, ok := ctx.Value(runIDKeyType{}).(string); ok {
		return v
	}
	return ""
}

// WithSessionID 在 ctx 上携带 chat session id，Handler 将其附加为 session.id 属性。
func WithSessionID(ctx context.Context, sessionID string) context.Context {
	return context.WithValue(ctx, sessionIDKeyType{}, sessionID)
}

// SessionIDFromContext 返回 ctx 上的 session id，无则返回空串。
func SessionIDFromContext(ctx context.Context) string {
	if v, ok := ctx.Value(sessionIDKeyType{}).(string); ok {
		return v
	}
	return ""
}

var (
	spanContextKey = spanContextKeyType{}
	_              = sdktrace.TracerProvider{} // keep sdktrace import meaningful across refactors
)

// spanContextValue 捆绑活跃 span、起始时间及流式回调需清理的 StreamReader。
type spanContextValue struct {
	span      trace.Span
	startTime time.Time
	// streamIn is the OnStartWithStreamInput copy the framework handed to
	// us; we must close it once we have read (or decided to skip) the
	// stream so the framework can recycle the original.
	streamIn *schema.StreamReader[callbacks.CallbackInput]
	// streamOut 为 OnEndWithStreamOutput 副本，同样须 close。
	streamOut *schema.StreamReader[callbacks.CallbackOutput]
}

// OtelHandler 实现 callbacks.Handler，将 eino 组件调用桥接为 OTel span。
//
// The handler is safe for concurrent use: it derives the per-call span
// from the provider's [trace.Tracer] (which is itself goroutine-safe) and
// stores the in-flight span on the callback context using an unexported
// key. OnEnd / OnError / streaming variants look up that key to finalise
// the span.
//
// A nil *OtelHandler.tp is treated as a no-op: every method returns the
// received context unchanged and creates no span. This makes it cheap to
// install the handler globally in environments that have not configured
// an OTel collector yet.
type OtelHandler struct {
	// tp is the provider that owns the tracer used to mint spans. nil
	// means "skip everything" — the handler becomes a transparent pass-
	// through.
	tp *sdktrace.TracerProvider

	// tracer is cached to avoid re-resolving it on every span start.
	// It is built lazily from tp so a nil tp does not panic.
	tracer   trace.Tracer
	initOnce sync.Once
}

// NewOtelHandler 将 tp 包装为 callbacks.Handler；nil tp 时透传不发射 span。
func NewOtelHandler(tp *sdktrace.TracerProvider) *OtelHandler {
	return &OtelHandler{tp: tp}
}

// resolveTracer 返回缓存 tracer；tp 为 nil 时回退全局 noop tracer。
func (h *OtelHandler) resolveTracer() trace.Tracer {
	h.initOnce.Do(func() {
		if h.tp == nil {
			h.tracer = noop.NewTracerProvider().Tracer(TracerName)
			return
		}
		h.tracer = h.tp.Tracer(TracerName)
	})
	return h.tracer
}

// spanName 构建 OTel span 名，约定 "<Component>:<Name>"；皆空时为 "component"。
func spanName(info *callbacks.RunInfo) string {
	if info == nil {
		return "component"
	}
	component := string(info.Component)
	name := info.Name
	switch {
	case component == "" && name == "":
		return "component"
	case component == "":
		return name
	case name == "":
		return component
	default:
		return component + ":" + name
	}
}

// runAttributes 返回 Handler 发射 span 的标准属性集（cpn.*/run.id/session.id）。
func runAttributes(info *callbacks.RunInfo, runID, sessionID string) []attribute.KeyValue {
	attrs := []attribute.KeyValue{
		attribute.String("run.id", runID),
		attribute.String("session.id", sessionID),
	}
	if info == nil {
		return attrs
	}
	// Canvas DSL loads each node with cpn_id as the node name, so
	// info.Name is a reliable cpn.id surrogate. We expose it under a
	// dedicated attribute so dashboards can filter by it directly.
	if info.Name != "" {
		attrs = append(attrs,
			attribute.String("cpn.id", info.Name),
			attribute.String("cpn.name", info.Name),
		)
	}
	if component := string(info.Component); component != "" {
		attrs = append(attrs, attribute.String("cpn.component", component))
	}
	if info.Type != "" {
		attrs = append(attrs, attribute.String("cpn.type", info.Type))
	}
	return attrs
}

// OnStart 为非流式组件调用入口：启动 span、附加属性并存入 context。
func (h *OtelHandler) OnStart(ctx context.Context, info *callbacks.RunInfo, _ callbacks.CallbackInput) context.Context {
	if h.tp == nil {
		return ctx
	}
	runID := RunIDFromContext(ctx)
	sessionID := SessionIDFromContext(ctx)
	startedCtx, span := h.resolveTracer().Start(ctx, spanName(info),
		trace.WithTimestamp(time.Now()),
		trace.WithAttributes(runAttributes(info, runID, sessionID)...),
	)
	return context.WithValue(startedCtx, spanContextKey, &spanContextValue{
		span:      span,
		startTime: time.Now(),
	})
}

// OnEnd 终结 OnStart 启动的 span；context 无 in-flight span 时为 no-op。
func (h *OtelHandler) OnEnd(ctx context.Context, info *callbacks.RunInfo, _ callbacks.CallbackOutput) context.Context {
	v, ok := ctx.Value(spanContextKey).(*spanContextValue)
	if !ok || v == nil {
		return ctx
	}
	// Drop the key so the same ctx is not reused for a different span
	// should eino (or a future refactor) ever share callbacks across
	// goroutines.
	v.span.End(trace.WithTimestamp(time.Now()))
	return context.WithValue(ctx, spanContextKey, (*spanContextValue)(nil))
}

// OnError 在 in-flight span 上记录错误并标记 Error 状态；无 span 时为 no-op。
func (h *OtelHandler) OnError(ctx context.Context, info *callbacks.RunInfo, err error) context.Context {
	if err == nil {
		// Treat nil error as a non-event so we never mark a span Error
		// when the framework calls us defensively.
		return ctx
	}
	v, ok := ctx.Value(spanContextKey).(*spanContextValue)
	if !ok || v == nil {
		return ctx
	}
	v.span.RecordError(err, trace.WithTimestamp(time.Now()))
	v.span.SetStatus(codes.Error, err.Error())
	v.span.End(trace.WithTimestamp(time.Now()))
	return context.WithValue(ctx, spanContextKey, (*spanContextValue)(nil))
}

// OnStartWithStreamInput 为流式输入镜像 OnStart；须 close 框架交付的 StreamReader 副本。
func (h *OtelHandler) OnStartWithStreamInput(ctx context.Context, info *callbacks.RunInfo,
	input *schema.StreamReader[callbacks.CallbackInput]) context.Context {
	if h.tp == nil {
		if input != nil {
			input.Close()
		}
		return ctx
	}
	runID := RunIDFromContext(ctx)
	sessionID := SessionIDFromContext(ctx)
	startedCtx, span := h.resolveTracer().Start(ctx, spanName(info),
		trace.WithTimestamp(time.Now()),
		trace.WithAttributes(runAttributes(info, runID, sessionID)...),
		trace.WithSpanKind(trace.SpanKindConsumer),
	)
	if input != nil {
		input.Close()
	}
	return context.WithValue(startedCtx, spanContextKey, &spanContextValue{
		span:      span,
		startTime: time.Now(),
		streamIn:  input,
	})
}

// OnEndWithStreamOutput 为流式输出镜像 OnEnd；close 输出流副本并终结 span。
func (h *OtelHandler) OnEndWithStreamOutput(ctx context.Context, info *callbacks.RunInfo,
	output *schema.StreamReader[callbacks.CallbackOutput]) context.Context {
	v, ok := ctx.Value(spanContextKey).(*spanContextValue)
	if !ok || v == nil {
		if output != nil {
			output.Close()
		}
		return ctx
	}
	if output != nil {
		output.Close()
		v.streamOut = output
	}
	v.span.End(trace.WithTimestamp(time.Now()))
	return context.WithValue(ctx, spanContextKey, (*spanContextValue)(nil))
}

// 编译期断言 *OtelHandler 满足 eino Handler 接口，即时捕获签名漂移。
var _ callbacks.Handler = (*OtelHandler)(nil)

// 保留 embedded 接口引用，升级 OTel 时若移除 trace/embedded 则编译报错。
var _ embedded.Tracer = (embedded.Tracer)(nil)
// otel/handler.go — eino 回调桥接 OTel span 的 Handler 实现。
