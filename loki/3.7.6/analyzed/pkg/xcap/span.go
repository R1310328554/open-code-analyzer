package xcap

// xcap 包 Span 嵌入 trace.Span 并可选关联 Region：End 时先把 Region 观测刷成 span 属性再结束底层 OTel span。

import (
	"go.opentelemetry.io/otel/trace"
)

// Span 通过嵌入委托 OTel API，仅重写 End/Record/Region 以衔接 xcap 观测管线。
// Span wraps a [trace.Span] and an optional [Region].
//
// All [trace.Span] methods are automatically delegated to the inner
// span via embedding. Only [End] is overridden to flush the Region's
// aggregated observations as span attributes before ending the inner
// span.
type Span struct {
	trace.Span
	region *Region
}

var _ trace.Span = (*Span)(nil)

// End flushes aggregated observations from the linked Region as span
// attributes, then ends the underlying OTel span.
//
// If no Region is linked (nil), or the Region has already been ended,
// End simply ends the inner span.
// End 先 flushToSpan 再调用内层 Span.End，Region 为 nil 或已结束时跳过刷新。
func (s *Span) End(options ...trace.SpanEndOption) {
	if s.region != nil {
		s.region.flushToSpan(s.Span)
	}
	s.Span.End(options...)
}

// Region returns the linked Region, or nil if no Region is attached.
func (s *Span) Region() *Region {
	if s == nil {
		return nil
	}

	return s.region
}

// Record records the given observation into the linked Region.
// Record 将观测写入关联 Region；Span 为 nil 时安全 no-op。
func (s *Span) Record(observation Observation) {
	if s == nil {
		return
	}

	s.region.Record(observation)
}
// var _ trace.Span 编译期断言确保 xcap.Span 满足 OTel Span 接口。
