package aws

// retryer 将 Grafana dskit backoff 策略映射到 AWS SDK v2 Retryer 接口，使 S3 重试延迟与 Cortex 一致并在 OpenTelemetry span 上记录重试次数。

import (
	"context"
	"time"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/aws/retry"
	"github.com/grafana/dskit/backoff"
	attribute "go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/trace"
)

// Map Cortex Backoff into AWS Retryer interface
// retryer 组合标准 AWS 重试器与 backoff.Backoff，MaxAttempts 由配置上限决定。
type retryer struct {
	aws.Retryer
	*backoff.Backoff
	maxRetries int
	context.Context
}

// newRetryer 用 MaxRetries 与 MaxBackoff 包装 Standard 重试策略。
func newRetryer(ctx context.Context, cfg backoff.Config) *retryer {
	return &retryer{
		retry.AddWithMaxBackoffDelay(retry.AddWithMaxAttempts(retry.NewStandard(), cfg.MaxRetries), cfg.MaxBackoff),
		backoff.New(ctx, cfg),
		cfg.MaxRetries, ctx}
}

// MaxAttempts is the number of times a request may be retried before
// failing.
func (r *retryer) MaxAttempts() int {
	return r.maxRetries
}

// RetryRules return the retry delay that should be used by the SDK before
// making another request attempt for the failed request.
// RetryDelay 从 backoff 取下一延迟并在 trace span 写入 retry 属性。
func (r *retryer) RetryDelay(_ int, _ error) (time.Duration, error) {
	duration := r.NextDelay()
	trace.SpanFromContext(r.Context).SetAttributes(attribute.Int("retry", r.NumRetries()))
	return duration, nil
}
// SDK 禁用内置重试后由 Loki 自行监控 S3 请求重试行为。
