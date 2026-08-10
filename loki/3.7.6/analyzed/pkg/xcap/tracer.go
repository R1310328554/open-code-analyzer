package xcap

// xcap 包 StartSpan 同时创建 OTel span 与同名 Region：返回 xcap.Span，End 时观测写入 trace 并参与 Capture 汇总。

import (
	"context"

	"go.opentelemetry.io/otel/trace"
)

// StartSpan 先 t.Start 再 StartRegion；无 Capture 时 Region 为 nil 但 span 仍可用。
// StartSpan creates a new OTel span using the given tracer and pairs it
// with a [Region] for aggregated observation recording.
//
// The returned [trace.Span] is an [xcap.Span] whose End method flushes
// Region observations as span attributes before ending the underlying
// OTel span.
//
// If a [Capture] is present in ctx, the Region is registered with it
// for summary aggregation via [SummaryLogValues]. If no Capture is
// found, a span is still created but no Region is attached (observation
// recording is a no-op).
//
// The Region is stored in the returned context and can be retrieved
// with [RegionFromContext] for recording observations.
// StartSpan 将 inner span 与 Region 包装为 xcap.Span 并更新 context。
func StartSpan(ctx context.Context, t trace.Tracer, name string, opts ...trace.SpanStartOption) (context.Context, *Span) {
	ctx, inner := t.Start(ctx, name, opts...)
	ctx, r := StartRegion(ctx, name)
	return ctx, &Span{Span: inner, region: r}
}
// Region 可通过 RegionFromContext 在 span 生命周期内追加 Record 观测。
