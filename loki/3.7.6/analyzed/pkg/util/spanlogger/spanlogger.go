package spanlogger

// spanlogger 包封装 dskit/spanlogger，注入 Loki tenant 解析器：NewOTel/FromContext 统一 trace 与 log，减少重复 kv 字段。

import (
	"context"

	"github.com/go-kit/log"
	"github.com/grafana/dskit/spanlogger" //lint:ignore faillint // This package is the wrapper that should be used.
	"github.com/grafana/dskit/tenant"
	"go.opentelemetry.io/otel/trace"
)

// resolverProxy 将 dskit tenant.TenantID/TenantIDs 适配为 spanlogger Resolver 接口。
type resolverProxy struct{}

func (r *resolverProxy) TenantID(ctx context.Context) (string, error) {
	return tenant.TenantID(ctx)
}

func (r *resolverProxy) TenantIDs(ctx context.Context) ([]string, error) {
	return tenant.TenantIDs(ctx)
}

var (
	resolver = &resolverProxy{}
)

// SpanLogger 类型别名指向 dskit 实现，支持 OTel span 与 log.Logger 联动输出。
// SpanLogger unifies tracing and logging, to reduce repetition.
type SpanLogger = spanlogger.SpanLogger

// NewOTel 创建带 method 名与 kvps 的子 span，ctx 可经 FromContext 取回 logger。
// NewOTel makes a new OTel SpanLogger with a log.Logger to send logs to. The provided context will have the logger attached
// to it and can be retrieved with FromContext.
func NewOTel(ctx context.Context, logger log.Logger, tracer trace.Tracer, method string, kvps ...interface{}) (*SpanLogger, context.Context) {
	return spanlogger.NewOTel(ctx, logger, tracer, method, resolver, kvps...)
}

// FromContext 无 parent span 时仅写 context logger，缺失则 fallback 到传入 logger。
// FromContext returns a span logger using the current parent span.
// If there is no parent span, the SpanLogger will only log to the logger
// within the context. If the context doesn't have a logger, the fallback
// logger is used.
func FromContext(ctx context.Context, fallback log.Logger) *SpanLogger {
	return spanlogger.FromContext(ctx, fallback, resolver)
}
// resolver 全局单例避免每次 NewOTel 分配，tenant 信息自动写入 span 属性。
