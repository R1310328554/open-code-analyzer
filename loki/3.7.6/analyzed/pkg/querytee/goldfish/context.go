package goldfish

// goldfish context 在 HTTP 处理链中传递采样决策，避免下游 FanOutHandler 重复执行 ShouldSample。

import "context"

// GoldfishCorrelationIDHeader 为关联采样请求与比较结果的 HTTP 头名。
// GoldfishCorrelationIDHeader is the HTTP header name for the goldfish correlation ID.
const GoldfishCorrelationIDHeader = "X-Loki-Goldfish-ID"

// SamplingDecision 记录上游是否采样及非空 correlationID。
// SamplingDecision represents an upstream goldfish sampling decision.
// Both positive and negative decisions are stored so downstream handlers
// do not re-evaluate sampling independently.
type SamplingDecision struct {
	Sampled       bool
	CorrelationID string // non-empty only when Sampled is true
}

type contextKey int

const samplingDecisionKey contextKey = iota

// ContextWithSamplingDecision 将采样决策写入 context 供 fanout 读取。
// ContextWithSamplingDecision stores a SamplingDecision in the context.
func ContextWithSamplingDecision(ctx context.Context, decision SamplingDecision) context.Context {
	return context.WithValue(ctx, samplingDecisionKey, decision)
}

// SamplingDecisionFromContext retrieves the SamplingDecision from context.
// Returns the decision and true if found, or a zero-value decision and false if not set.
func SamplingDecisionFromContext(ctx context.Context) (SamplingDecision, bool) {
	decision, ok := ctx.Value(samplingDecisionKey).(SamplingDecision)
	return decision, ok
}
// SamplingDecisionFromContext 未设置时返回 false，FanOutHandler 将自行采样。
