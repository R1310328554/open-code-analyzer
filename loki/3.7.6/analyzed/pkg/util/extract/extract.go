package extract

// extract 包从 Prometheus 风格 matchers 或 logproto 标签中提取 __name__ 指标名，供 ruler 与查询引擎拆分指标名与其余标签过滤器。

import (
	"fmt"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/logproto"
)

var errNoMetricNameLabel = fmt.Errorf("no metric name label")

// MetricNameMatcherFromMatchers 分离 __name__ matcher 并返回剩余 matchers 副本。
// MetricNameMatcherFromMatchers extracts the metric name from a set of matchers
func MetricNameMatcherFromMatchers(matchers []*labels.Matcher) (*labels.Matcher, []*labels.Matcher, bool) {
	// Handle the case where there is no metric name and all matchers have been
	// filtered out e.g. {foo=""}.
	if len(matchers) == 0 {
		return nil, matchers, false
	}

	outMatchers := make([]*labels.Matcher, len(matchers)-1)
	for i, matcher := range matchers {
		if matcher.Name != model.MetricNameLabel {
			continue
		}

		// Copy other matchers, excluding the found metric name matcher
		copy(outMatchers, matchers[:i])
		copy(outMatchers[i:], matchers[i+1:])
		return matcher, outMatchers, true
	}
	// Return all matchers if none are metric name matchers
	return nil, matchers, false
}

// UnsafeMetricNameFromLabelAdapters 返回标签值字符串引用，无拷贝但调用方不得修改底层数据。
// UnsafeMetricNameFromLabelAdapters extracts the metric name from a list of LabelPairs.
// The returned metric name string is a reference to the label value (no copy).
func UnsafeMetricNameFromLabelAdapters(labels []logproto.LabelAdapter) (string, error) {
	for _, label := range labels {
		if label.Name == model.MetricNameLabel {
			return label.Value, nil
		}
	}
	return "", errNoMetricNameLabel
}
// matchers 为空时 MetricNameMatcherFromMatchers 直接返回 false，对应 {foo=""} 等退化选择器。
