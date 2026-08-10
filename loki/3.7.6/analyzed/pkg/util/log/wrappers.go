package log

// log 包 wrappers 在 logger 上下文中注入 org_id 与 traceID 字段，使多租户与分布式追踪信息自动出现在结构化日志中。

import (
	"context"

	"github.com/go-kit/log"
	"github.com/grafana/dskit/tracing"

	"github.com/grafana/dskit/tenant"
)

// WithUserID 添加 org_id 键，与 Cortex 历史命名保持一致便于日志关联。
// WithUserID returns a Logger that has information about the current user in
// its details.
func WithUserID(userID string, l log.Logger) log.Logger {
	// See note in WithContext.
	return log.With(l, "org_id", userID)
}

// WithContext 从 context 提取 tenant 与采样 traceID，sampled 时附加 sampled=true。
// WithContext returns a log.Logger that has information about the current user in
// its details.
//
// e.g.
//
//	log := util.WithContext(ctx)
//	log.Errorf("Could not chunk chunks: %v", err)
func WithContext(ctx context.Context, l log.Logger) log.Logger {
	// Weaveworks uses "orgs" and "orgID" to represent Cortex users,
	// even though the code-base generally uses `userID` to refer to the same thing.
	userID, err := tenant.TenantID(ctx)
	if err == nil {
		l = WithUserID(userID, l)
	}

	traceID, sampled := tracing.ExtractSampledTraceID(ctx)
	if sampled {
		return log.With(l, "traceID", traceID, "sampled", "true")
	}
	if traceID != "" {
		return log.With(l, "traceID", traceID)
	}
	return l

}
// tenant 解析失败时仅省略 org_id，trace 为空则原样返回底层 logger。
