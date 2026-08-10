package queryrange

// split_by_range 对 instant metric 查询按 range vector 区间拆分子查询，可选 splitAlign 与 splitByInterval 对齐。

import (
	"context"
	"fmt"
	"net/http"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/httpgrpc"
	"github.com/grafana/dskit/tenant"
	"github.com/prometheus/prometheus/promql/parser"

	"github.com/grafana/loki/v3/pkg/loghttp"
	"github.com/grafana/loki/v3/pkg/logql"
	"github.com/grafana/loki/v3/pkg/logqlmodel/stats"
	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
	"github.com/grafana/loki/v3/pkg/util/marshal"
	"github.com/grafana/loki/v3/pkg/util/validation"
)

// splitByRange 使用 RangeMapper 改写 AST 并通过 DownstreamEngine 执行子查询。
type splitByRange struct {
	logger  log.Logger
	next    queryrangebase.Handler
	limits  Limits
	ng      *logql.DownstreamEngine
	metrics *logql.MapperMetrics

	// splitAlign 为 true 时子查询时间窗口与 frontend 全局 split 边界对齐。
// Whether to align rangeInterval align to splitByInterval in the subqueries.
	splitAlign bool
}

// NewSplitByRangeMiddleware 在 InstantMetricQuerySplitDuration 为 0 时透传。
// NewSplitByRangeMiddleware creates a new Middleware that splits log requests by the range interval.
func NewSplitByRangeMiddleware(logger log.Logger, engineOpts logql.EngineOpts, limits Limits, splitAlign bool, metrics *logql.MapperMetrics) queryrangebase.Middleware {
	return queryrangebase.MiddlewareFunc(func(next queryrangebase.Handler) queryrangebase.Handler {
		return &splitByRange{
			logger: log.With(logger, "middleware", "InstantQuery.splitByRangeVector"),
			next:   next,
			limits: limits,
			ng: logql.NewDownstreamEngine(engineOpts, DownstreamHandler{
				limits:     limits,
				next:       next,
				splitAlign: splitAlign,
			}, limits, logger),
			metrics:    metrics,
			splitAlign: splitAlign,
		}
	})
}

// Do 仅处理 LokiInstantRequest，将 mapper stats 合并进最终响应 Statistics。
func (s *splitByRange) Do(ctx context.Context, request queryrangebase.Request) (queryrangebase.Response, error) {
	logger := util_log.WithContext(ctx, s.logger)

	params, err := ParamsFromRequest(request)
	if err != nil {
		return nil, err
	}

	tenants, err := tenant.TenantIDs(ctx)
	if err != nil {
		return nil, httpgrpc.Errorf(http.StatusBadRequest, "%s", err.Error())
	}

	interval := validation.SmallestPositiveNonZeroDurationPerTenant(tenants, s.limits.InstantMetricQuerySplitDuration)
	// if no interval configured, continue to the next middleware
	if interval == 0 {
		return s.next.Do(ctx, request)
	}

	mapperStats := logql.NewMapperStats()

	ir, ok := request.(*LokiInstantRequest)
	if !ok {
		return nil, fmt.Errorf("expected *LokiInstantRequest, got %T", request)
	}

	var mapper logql.RangeMapper

	if s.splitAlign {
		mapper, err = logql.NewRangeMapperWithSplitAlign(interval, ir.TimeTs, s.metrics, mapperStats)
	} else {
		mapper, err = logql.NewRangeMapper(interval, s.metrics, mapperStats)
	}
	if err != nil {
		return nil, err
	}

	noop, parsed, err := mapper.Parse(params.GetExpression())
	if err != nil {
		level.Warn(logger).Log("msg", "failed mapping AST", "err", err.Error(), "query", request.GetQuery())
		return nil, err
	}
	level.Debug(logger).Log("msg", "mapped instant query", "interval", interval.String(), "noop", noop, "original", request.GetQuery(), "mapped", parsed.String())

	if noop {
		// the query cannot be split, so continue
		return s.next.Do(ctx, request)
	}

	// Update middleware stats
	queryStatsCtx := stats.FromContext(ctx)
	queryStatsCtx.AddSplitQueries(int64(mapperStats.GetSplitQueries()))

	query := s.ng.Query(ctx, logql.ParamsWithExpressionOverride{Params: params, ExpressionOverride: parsed})

	res, err := query.Exec(ctx)
	if err != nil {
		return nil, err
	}

	value, err := marshal.NewResultValue(res.Data)
	if err != nil {
		return nil, err
	}

	switch res.Data.Type() {
	case parser.ValueTypeMatrix:
		return &LokiPromResponse{
			Response: &queryrangebase.PrometheusResponse{
				Status: loghttp.QueryStatusSuccess,
				Data: queryrangebase.PrometheusData{
					ResultType: loghttp.ResultTypeMatrix,
					Result:     toProtoMatrix(value.(loghttp.Matrix)),
				},
			},
			Statistics: res.Statistics,
		}, nil
	case parser.ValueTypeVector:
		return &LokiPromResponse{
			Response: &queryrangebase.PrometheusResponse{
				Status: loghttp.QueryStatusSuccess,
				Data: queryrangebase.PrometheusData{
					ResultType: loghttp.ResultTypeVector,
					Result:     toProtoVector(value.(loghttp.Vector)),
				},
			},
			Statistics: res.Statistics,
		}, nil
	default:
		return nil, fmt.Errorf("unexpected downstream response type (%T)", res.Data.Type())
	}
}
// DownstreamHandler 携带 splitAlign 标志，剥离 offset 并平移子查询时间范围。
