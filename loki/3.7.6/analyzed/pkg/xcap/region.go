package xcap

// xcap 包 Region 表示一次 Capture 内子操作的观测窗口：可独立 StartRegion 收集指标，或与 Tracer.Start 配对写入 OTel span 属性。

import (
	"context"
	"fmt"
	"sync"

	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/trace"
)

// Region 维护 parent/child 树与 StatisticKey→AggregatedObservation 映射，End 后拒绝写入。
// Region captures the lifetime of a specific operation within a capture.
//
// A Region may be created standalone (via [StartRegion]) for observation
// collection only, or paired with an OTel span via [Tracer.Start].
type Region struct {
	// name is the name of the region.
	name string

	// id is the unique identifier of the region.
	id identifier

	// parentID is the ID of the parent region. Set to zero value if root region.
	parentID identifier

	// mu protects the fields below.
	mu sync.RWMutex

	// observations are all observations recorded in this region.
	// Map from statistic key to aggregated observation value.
	observations map[StatisticKey]*AggregatedObservation

	// ended indicates whether End() has been called.
	ended bool
}

// StartRegion creates a new Region to record observations for a specific operation.
// It returns the new Region and a context containing the Region.
//
// The Region is registered with the [Capture] found in the context.
// If no Capture is found, it returns the original context and a nil region.
//
// StartRegion does not create an OTel span. Use [Tracer.Start] when both
// a span and observation aggregation are needed.
// StartRegion 无 Capture 时返回 nil Region；否则注册到 Capture 并注入 context。
func StartRegion(ctx context.Context, name string) (context.Context, *Region) {
	capture := CaptureFromContext(ctx)
	if capture == nil {
		return ctx, nil
	}

	r := &Region{
		id:           newID(),
		name:         name,
		observations: make(map[StatisticKey]*AggregatedObservation),
	}

	// extract parentID from context
	if pr := RegionFromContext(ctx); pr != nil {
		r.parentID = pr.id
	}

	// Add region to capture.
	capture.AddRegion(r)

	// Update context with the new region.
	return ContextWithRegion(ctx, r), r
}

// Record records the statistic Observation o into the region. Calling
// Record multiple times for the same Statistic aggregates values based
// on the aggregation type of the Statistic.
// Record 对同 key 多次观测按 Sum/Min/Max/First/Last 规则合并到 AggregatedObservation。
func (r *Region) Record(o Observation) {
	if r == nil {
		return
	}

	r.mu.Lock()
	defer r.mu.Unlock()

	if r.ended {
		return
	}

	key := o.statistic().Key()
	if _, ok := r.observations[key]; !ok {
		// First observation for this statistic.
		r.observations[key] = &AggregatedObservation{
			Statistic: o.statistic(),
			Value:     o.value(),
			Count:     1,
		}
		return
	}

	// Aggregate with existing observations.
	agg := r.observations[key]
	agg.Record(o)
}

// Observations returns all aggregated observations recorded in the region.
func (r *Region) Observations() []AggregatedObservation {
	r.mu.RLock()
	defer r.mu.RUnlock()

	observations := make([]AggregatedObservation, 0, len(r.observations))
	for _, agg := range r.observations {
		observations = append(observations, *agg)
	}

	return observations
}

// End completes the Region. Updates to the Region are ignored after
// calling End.
func (r *Region) End() {
	if r == nil {
		return
	}

	r.mu.Lock()
	defer r.mu.Unlock()

	r.ended = true
}

// flushToSpan converts aggregated observations to OTel span attributes
// and sets them on the provided span. It also marks the region as ended.
//
// This is called by [Span.End].
// flushToSpan 将聚合结果转为 OTel attribute 并标记 ended，供 Span.End 调用。
func (r *Region) flushToSpan(span trace.Span) {
	if r == nil {
		return
	}

	r.mu.Lock()
	defer r.mu.Unlock()

	if r.ended {
		return
	}

	attrs := make([]attribute.KeyValue, 0, len(r.observations))
	for key, obs := range r.observations {
		attrs = append(attrs, observationToAttribute(key, obs))
	}
	if len(attrs) > 0 {
		span.SetAttributes(attrs...)
	}

	r.ended = true
}

// observationToAttribute converts an aggregated observation to an
// OpenTelemetry span attribute. The attribute key is the statistic name
// and the value type matches the statistic's data type.
func observationToAttribute(key StatisticKey, obs *AggregatedObservation) attribute.KeyValue {
	attrKey := attribute.Key(key.Name)

	switch key.DataType {
	case DataTypeInt64:
		if val, ok := obs.Value.(int64); ok {
			return attrKey.Int64(val)
		}
	case DataTypeFloat64:
		if val, ok := obs.Value.(float64); ok {
			return attrKey.Float64(val)
		}
	case DataTypeBool:
		if val, ok := obs.Value.(bool); ok {
			return attrKey.Bool(val)
		}
	}

	// Fallback: convert to string.
	return attrKey.String(fmt.Sprintf("%v", obs.Value))
}
// observationToAttribute 按 DataType 选择 Int64/Float64/Bool，无法转换时 fallback 字符串。
