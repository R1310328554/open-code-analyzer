package ruler

// LocalEvaluator 在 ruler 进程内用 logql.QueryEngine 本地执行规则查询，EvalModeLocal 模式下无需远程 query-frontend。

import (
	"context"
	"fmt"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logql"
	"github.com/grafana/loki/v3/pkg/logqlmodel"
	"github.com/grafana/loki/v3/pkg/util"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

const EvalModeLocal = "local"

// LocalEvaluator 持有 QueryEngine 与 insightsLogger，记录规则执行耗时与字节数。
type LocalEvaluator struct {
	engine *logql.QueryEngine
	logger log.Logger

	// we don't want/need to log all the additional context, such as
	// caller=spanlogger.go:116 component=ruler evaluation_mode=remote method=ruler.remoteEvaluation.Query
	// in insights logs, so create a new logger
	insightsLogger log.Logger
}

// NewLocalEvaluator 校验 engine 非 nil 并构造 insights 专用 logger。
func NewLocalEvaluator(engine *logql.QueryEngine, logger log.Logger) (*LocalEvaluator, error) {
	if engine == nil {
		return nil, fmt.Errorf("given engine is nil")
	}

	return &LocalEvaluator{
		engine:         engine,
		logger:         logger,
		insightsLogger: log.With(util_log.Logger, "msg", "request timings", "insight", "true", "source", "loki_ruler"),
	}, nil
}

// Eval 用 NewLiteralParams 构造 instant 查询，Exec 后写 insights 日志含 rule_name。
func (l *LocalEvaluator) Eval(ctx context.Context, qs string, now time.Time) (*logqlmodel.Result, error) {
	params, err := logql.NewLiteralParams(
		qs,
		now,
		now,
		0,
		0,
		logproto.FORWARD,
		0,
		nil,
		nil,
	)
	if err != nil {
		return nil, err
	}

	q := l.engine.Query(params)
	res, err := q.Exec(ctx)
	if err != nil {
		return nil, err
	}

	// Retrieve rule details from context
	ruleName, ruleType := GetRuleDetailsFromContext(ctx)

	level.Info(l.insightsLogger).Log("rule_name", ruleName, "rule_type", ruleType, "total", res.Statistics.Summary.ExecTime, "total_bytes", res.Statistics.Summary.TotalBytesProcessed, "query_hash", util.HashedQuery(qs))
	return &res, nil
}
// GetRuleDetailsFromContext 从 context 提取规则名与类型供可观测性打点。
