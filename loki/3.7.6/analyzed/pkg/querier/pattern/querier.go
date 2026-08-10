package pattern

// pattern 包 querier 定义 PatterQuerier 接口及 MergePatternResponses，合并多源模式查询 Series。

import (
	"context"
	"sort"

	"github.com/grafana/loki/v3/pkg/logproto"
)

type PatterQuerier interface {
	Patterns(ctx context.Context, req *logproto.QueryPatternsRequest) (*logproto.QueryPatternsResponse, error)
}

// MergePatternResponses 按 pattern 字符串合并样本，各 series 内按 Timestamp 排序。
func MergePatternResponses(responses []*logproto.QueryPatternsResponse) *logproto.QueryPatternsResponse {
	if len(responses) == 0 {
		return &logproto.QueryPatternsResponse{
			Series: []*logproto.PatternSeries{},
		}
	}

	if len(responses) == 1 {
		return responses[0]
	}

	// Merge patterns by pattern string
	patternMap := make(map[string]*logproto.PatternSeries)

	for _, resp := range responses {
		if resp == nil {
			continue
		}

		for _, series := range resp.Series {
			existing, exists := patternMap[series.Pattern]
			if !exists {
				patternMap[series.Pattern] = series
				continue
			}

			// Merge samples
			existing.Samples = append(existing.Samples, series.Samples...)
		}
	}

	// Sort samples within each series by timestamp
	result := &logproto.QueryPatternsResponse{
		Series: make([]*logproto.PatternSeries, 0, len(patternMap)),
	}

	for _, series := range patternMap {
		// Sort samples by timestamp
		sort.Slice(series.Samples, func(i, j int) bool {
			return series.Samples[i].Timestamp < series.Samples[j].Timestamp
		})
		result.Series = append(result.Series, series)
	}

	return result
}
// 空或单响应快速返回，map 键为 pattern 文本以去重跨 ingester 的重复模式。
