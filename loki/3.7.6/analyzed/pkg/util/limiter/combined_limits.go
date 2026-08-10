package limiter

// limiter.CombinedLimits 聚合 Loki 各子系统 Limits 接口，供单配置结构体统一注入限流策略。

import (
	bloombuilder "github.com/grafana/loki/v3/pkg/bloombuild/builder"
	bloomplanner "github.com/grafana/loki/v3/pkg/bloombuild/planner"
	"github.com/grafana/loki/v3/pkg/bloomgateway"
	"github.com/grafana/loki/v3/pkg/compactor"
	"github.com/grafana/loki/v3/pkg/distributor"
	"github.com/grafana/loki/v3/pkg/indexgateway"
	"github.com/grafana/loki/v3/pkg/ingester"
	"github.com/grafana/loki/v3/pkg/pattern"
	querier_limits "github.com/grafana/loki/v3/pkg/querier/limits"
	queryrange_limits "github.com/grafana/loki/v3/pkg/querier/queryrange/limits"
	"github.com/grafana/loki/v3/pkg/ruler"
	scheduler_limits "github.com/grafana/loki/v3/pkg/scheduler/limits"
	"github.com/grafana/loki/v3/pkg/storage"
	"github.com/grafana/loki/v3/pkg/storage/bucket"
)

// CombinedLimits 嵌入 compactor/distributor/ingester/querier/ruler 等组件的 Limits 约束。
type CombinedLimits interface {
	compactor.Limits
	distributor.Limits
	ingester.Limits
	querier_limits.Limits
	queryrange_limits.Limits
	ruler.RulesLimits
	scheduler_limits.Limits
	storage.StoreLimits
	indexgateway.Limits
	bloomgateway.Limits
	bloomplanner.Limits
	bloombuilder.Limits
	pattern.Limits
	bucket.SSEConfigProvider
}
// 同时覆盖 bloomgateway、bloombuild、pattern、indexgateway 与 bucket SSE 等扩展模块限额。
