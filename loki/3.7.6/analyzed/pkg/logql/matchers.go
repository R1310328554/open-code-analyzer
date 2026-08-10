package logql

// matchers 解析 series API 请求中的多组标签 matcher 字符串。

import (
	"github.com/pkg/errors"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/logql/syntax"
)

// MatchForSeriesRequest 逐组解析 matcher 字符串；series 查询允许空 matcher 组但每组非空。
// MatchForSeriesRequest extracts and parses multiple matcher groups from a slice of strings.
// Does not perform validation as it's used for series queries
// which allow empty matchers
func MatchForSeriesRequest(xs []string) ([][]*labels.Matcher, error) {
	groups := make([][]*labels.Matcher, 0, len(xs))
	for _, x := range xs {
		ms, err := syntax.ParseMatchers(x, false)
		if err != nil {
			return nil, err
		}
		if len(ms) == 0 {
			return nil, errors.Errorf("0 matchers in group: %s", x)
		}
		groups = append(groups, ms)
	}

	return groups, nil
}
// 解析失败或某组 matcher 为空时返回错误，由 HTTP 层转换为 400 响应。
