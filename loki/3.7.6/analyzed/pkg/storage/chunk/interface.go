// This file was taken from Prometheus (https://github.com/prometheus/prometheus).
// The original license header is included below:
//
// Copyright 2014 The Prometheus Authors
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

// chunk 包核心接口定义：Data 表示可追加/序列化的 chunk 载荷，Filterer 用于按标签过滤查询涉及的 chunk。

package chunk

import (
	"context"
	"errors"
	"io"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/util/filter"
)

// ChunkLen 为 chunk 字节长度常量（1024），供序列化布局参考。
// ChunkLen is the length of a chunk in bytes.
const ChunkLen = 1024

var (
	ErrRewriteNoDataLeft  = errors.New("chunk has no data left after rewriting")
	ErrSliceChunkOverflow = errors.New("slicing should not overflow a chunk")
)

// Data 定义 chunk 通用行为：追加样本、编解码、Rewrite 过滤及容量统计，非 goroutine 安全。
// Data is the interface for all chunks. Chunks are generally not
// goroutine-safe.
type Data interface {
	// Add adds a SamplePair to the chunks, performs any necessary
	// re-encoding, and creates any necessary overflow chunk.
	// The returned Chunk is the overflow chunk if it was created.
	// The returned Chunk is nil if the sample got appended to the same chunk.
	Add(sample model.SamplePair) (Data, error)
	Marshal(io.Writer) error
	UnmarshalFromBuf([]byte) error
	Encoding() Encoding
	// Rewrite rewrites the chunk after filtering out lines based on response from filter.Func.
	// Filter.Func would be called for each log entry, and the ones for which it returns true would be removed.
	Rewrite(filter filter.Func) (Data, error)
	// Size returns the approximate length of the chunk in bytes.
	Size() int
	// UncompressedSize returns the length of uncompressed bytes.
	UncompressedSize() int
	// Entries returns the number of entries in a chunk
	Entries() int
	Utilization() float64
}

// RequestChunkFilterer creates ChunkFilterer for a given request context.
// RequestChunkFilterer 按请求 context 构造 Filterer，实现 per-request 过滤策略。
type RequestChunkFilterer interface {
	ForRequest(ctx context.Context) Filterer
}

// Filterer filters chunks based on the metric.
// Filterer 根据 metric 标签决定是否跳过 chunk，RequiredLabelNames 声明依赖的标签键。
type Filterer interface {
	ShouldFilter(metric labels.Labels) bool
	RequiredLabelNames() []string
}
// ErrRewriteNoDataLeft 与 ErrSliceChunkOverflow 表示 Rewrite 或切片后 chunk 无效的边界情况。
