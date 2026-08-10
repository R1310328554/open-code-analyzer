package limits

// limits 包定义 querier 侧租户限制接口：组合 logql、pattern 与时间范围、超时、matcher 数量等约束。

import (
	"context"
	"time"

	"github.com/grafana/loki/v3/pkg/logql"
	"github.com/grafana/loki/v3/pkg/pattern"
)

type TimeRangeLimits interface {
	MaxQueryLookback(context.Context, string) time.Duration
	MaxQueryLength(context.Context, string) time.Duration
}

// Limits 嵌入 logql.Limits、pattern.Limits 及 querier 专属条目/标签/Tail 并发限制。
type Limits interface {
	logql.Limits
	pattern.Limits
	TimeRangeLimits
	QueryTimeout(context.Context, string) time.Duration
	MaxStreamsMatchersPerQuery(context.Context, string) int
	MaxConcurrentTailRequests(context.Context, string) int
	MaxEntriesLimitPerQuery(context.Context, string) int
	RequiredLabels(context.Context, string) []string
	RequiredNumberLabels(context.Context, string) int
}
// RequiredLabels/RequiredNumberLabels 强制查询携带指定标签以满足多租户隔离策略。
