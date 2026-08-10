package iter

// iter 包 batch 从 Iterator 批量读取 PatternSample，组装为 QueryPatternsResponse 的 Series 切片。

import (
	"math"

	"github.com/grafana/loki/v3/pkg/logproto"
)

func ReadBatch(it Iterator, batchSize int) (*logproto.QueryPatternsResponse, error) {
	var (
		series   = map[string]map[string][]*logproto.PatternSample{}
		respSize int
	)

	for ; respSize < batchSize && it.Next(); respSize++ {
		pattern := it.Pattern()
		lvl := it.Level()
		sample := it.At()

		if _, ok := series[lvl]; !ok {
			series[lvl] = map[string][]*logproto.PatternSample{}
		}
		series[lvl][pattern] = append(series[lvl][pattern], &sample)
	}
	result := logproto.QueryPatternsResponse{
		Series: make([]*logproto.PatternSeries, 0, len(series)),
	}
	for lvl, patterns := range series {
		for pattern, samples := range patterns {
			result.Series = append(result.Series, &logproto.PatternSeries{
				Pattern: pattern,
				Level:   lvl,
				Samples: samples,
			})
		}
	}
	return &result, it.Err()
}

// ReadAll 等价于 ReadBatch(it, MaxInt32)，一次性耗尽迭代器。
func ReadAll(it Iterator) (*logproto.QueryPatternsResponse, error) {
	return ReadBatch(it, math.MaxInt32)
}
// Merge/NewQueryClientIterator 等组合器与 ReadBatch 配合实现跨 ingester 查询合并。
