package deletion

// deletion 包将 logproto.Delete 请求转换为 log.PipelineFilter，在日志查询与采样提取管线中叠加按时间范围的删除过滤。

import (
	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logql"
	"github.com/grafana/loki/v3/pkg/logql/log"
	"github.com/grafana/loki/v3/pkg/logql/syntax"
)

func SetupPipeline(req logql.SelectLogParams, p log.Pipeline) (log.Pipeline, error) {
	if len(req.Deletes) == 0 {
		return p, nil
	}

	filters, err := deleteFilters(req.Deletes)
	if err != nil {
		return nil, err
	}

	return log.NewFilteringPipeline(filters, p), nil
}

// SetupExtractor 为指标采样路径注入相同删除过滤器，保持日志与样本视图一致。
func SetupExtractor(req logql.QueryParams, se log.SampleExtractor) (log.SampleExtractor, error) {
	if len(req.GetDeletes()) == 0 {
		return se, nil
	}

	filters, err := deleteFilters(req.GetDeletes())
	if err != nil {
		return nil, err
	}

	return log.NewFilteringSampleExtractor(filters, se), nil
}

// deleteFilters 解析每条 Delete 的 LogQL 选择器并绑定 Start/End 时间窗。
func deleteFilters(deletes []*logproto.Delete) ([]log.PipelineFilter, error) {
	var filters []log.PipelineFilter
	for _, d := range deletes {
		expr, err := syntax.ParseLogSelector(d.Selector, true)
		if err != nil {
			return nil, err
		}

		pipeline, err := expr.Pipeline()
		if err != nil {
			return nil, err
		}

		filters = append(filters, log.PipelineFilter{
			Start:    d.Start,
			End:      d.End,
			Matchers: expr.Matchers(),
			Pipeline: pipeline,
		})
	}

	return filters, nil
}
// PipelineFilter 携带 Matchers 与 Pipeline，便于 compactor 与 querier 复用同一过滤语义。
