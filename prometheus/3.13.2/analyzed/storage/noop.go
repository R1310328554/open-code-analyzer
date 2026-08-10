// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// storage 空实现（noop）Querier/SeriesSet：用于 fanout 过滤无效 secondary或测试占位，Select 恒为空且不产生错误。

package storage

import (
	"context"

	"github.com/prometheus/prometheus/model/labels"
	"github.com/prometheus/prometheus/util/annotations"
)

type noopQuerier struct{}

// NoopQuerier 返回空 SeriesSet，Label* 为空列表，Close 无操作。
// NoopQuerier is a Querier that does nothing.
func NoopQuerier() Querier {
	return noopQuerier{}
}

func (noopQuerier) Select(context.Context, bool, *SelectHints, ...*labels.Matcher) SeriesSet {
	return NoopSeriesSet()
}

func (noopQuerier) LabelValues(context.Context, string, *LabelHints, ...*labels.Matcher) ([]string, annotations.Annotations, error) {
	return nil, nil, nil
}

func (noopQuerier) LabelNames(context.Context, *LabelHints, ...*labels.Matcher) ([]string, annotations.Annotations, error) {
	return nil, nil, nil
}

func (noopQuerier) Close() error {
	return nil
}

func (noopQuerier) SearchLabelNames(_ context.Context, _ *SearchHints, _ ...*labels.Matcher) SearchResultSet {
	return nil
}

func (noopQuerier) SearchLabelValues(_ context.Context, _ string, _ *SearchHints, _ ...*labels.Matcher) SearchResultSet {
	return nil
}

var _ Searcher = noopQuerier{}

type noopChunkQuerier struct{}

// NoopChunkedQuerier 为 chunk 查询路径的空实现，供 merge/fanout 跳过。
// NoopChunkedQuerier is a ChunkQuerier that does nothing.
func NoopChunkedQuerier() ChunkQuerier {
	return noopChunkQuerier{}
}

func (noopChunkQuerier) Select(context.Context, bool, *SelectHints, ...*labels.Matcher) ChunkSeriesSet {
	return NoopChunkedSeriesSet()
}

func (noopChunkQuerier) LabelValues(context.Context, string, *LabelHints, ...*labels.Matcher) ([]string, annotations.Annotations, error) {
	return nil, nil, nil
}

func (noopChunkQuerier) LabelNames(context.Context, *LabelHints, ...*labels.Matcher) ([]string, annotations.Annotations, error) {
	return nil, nil, nil
}

func (noopChunkQuerier) Close() error {
	return nil
}

type noopSeriesSet struct{}

// NoopSeriesSet 永不 Next，At 返回 nil，用于表示无匹配序列。
// NoopSeriesSet is a SeriesSet that does nothing.
func NoopSeriesSet() SeriesSet {
	return noopSeriesSet{}
}

func (noopSeriesSet) Next() bool { return false }

func (noopSeriesSet) At() Series { return nil }

func (noopSeriesSet) Err() error { return nil }

func (noopSeriesSet) Warnings() annotations.Annotations { return nil }

type noopChunkedSeriesSet struct{}

// NoopChunkedSeriesSet 为空的 ChunkSeriesSet，与 NoopSeriesSet 语义对称。
// NoopChunkedSeriesSet is a ChunkSeriesSet that does nothing.
func NoopChunkedSeriesSet() ChunkSeriesSet {
	return noopChunkedSeriesSet{}
}

func (noopChunkedSeriesSet) Next() bool { return false }

func (noopChunkedSeriesSet) At() ChunkSeries { return nil }

func (noopChunkedSeriesSet) Err() error { return nil }

func (noopChunkedSeriesSet) Warnings() annotations.Annotations { return nil }
