package xcap

// xcap 包 context 子模块通过私有 ctxKeyType 在 context 中携带 Capture 与 Region：避免与标准库或其他包 WithValue 键冲突。

import (
	"context"

	"go.opentelemetry.io/otel/trace"
)

type ctxKeyType string

const (
	captureKey ctxKeyType = "capture"
	regionKey  ctxKeyType = "region"
)

// CaptureFromContext 类型断言失败返回 nil，调用方应处理无 Capture 场景。
// CaptureFromContext returns the Capture from the context, or nil if no Capture
// is present.
func CaptureFromContext(ctx context.Context) *Capture {
	v, ok := ctx.Value(captureKey).(*Capture)
	if !ok {
		return nil
	}
	return v
}

// contextWithCapture returns a new context with the given Capture.
func contextWithCapture(ctx context.Context, capture *Capture) context.Context {
	return context.WithValue(ctx, captureKey, capture)
}

// RegionFromContext 返回当前活跃 Region，StartRegion/StartSpan 会注入。
// RegionFromContext returns the current Region from the context, or nil if no Region
// is present.
func RegionFromContext(ctx context.Context) *Region {
	v, ok := ctx.Value(regionKey).(*Region)
	if !ok {
		return nil
	}
	return v
}

// ContextWithRegion returns a new context with the given Region.
func ContextWithRegion(ctx context.Context, region *Region) context.Context {
	return context.WithValue(ctx, regionKey, region)
}

// ContextWithSpan 若为 xcap Span 且关联 Region，同时注入 Region 供深层 Record。
// ContextWithSpan injects span into ctx via [trace.ContextWithSpan].
// If span is an [*Span] with a linked [Region], the Region is also
// injected so that [RegionFromContext] returns it downstream.
func ContextWithSpan(ctx context.Context, span trace.Span) context.Context {
	ctx = trace.ContextWithSpan(ctx, span)
	if s, ok := span.(*Span); ok && s.region != nil {
		ctx = ContextWithRegion(ctx, s.region)
	}
	return ctx
}
// captureKey/regionKey 使用私有 string 类型键，符合 Go context 最佳实践。
