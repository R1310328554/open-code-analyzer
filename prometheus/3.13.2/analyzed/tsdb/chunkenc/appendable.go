// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 直方图 appendable 接口子集：供 AppendHistogram/AppendFloatHistogram 检测跨 chunk 的计数器重置与 bucket 布局兼容性，避免调用方强制类型断言。

package chunkenc

import "github.com/prometheus/prometheus/model/histogram"

// histogramAppendable 抽象整数直方图 appender 的 appendable 方法，供跨 chunk 衔接。
// histogramAppendable is the subset of an integer-histogram appender's API
// that AppendHistogram needs from a previous chunk's appender in order to
// detect counter resets across chunks. It is satisfied by *HistogramAppender
// and any type that embeds it (e.g. *HistogramSTAppender). The Appender
// interface itself stays a single type so callers can hand off whichever
// concrete appender they happen to have without first type-asserting it.
type histogramAppendable interface {
	appendable(*histogram.Histogram) (
		positiveInserts, negativeInserts,
		backwardPositiveInserts, backwardNegativeInserts []Insert,
		okToAppend bool, counterResetHint CounterResetHeader,
	)
}

// floatHistogramAppendable 为浮点直方图版本，counter reset 以 bool 而非 CounterResetHeader 表示。
// floatHistogramAppendable is the float-histogram counterpart of
// histogramAppendable. Note the asymmetry with the integer version: float
// histograms surface a bool counter-reset flag rather than the richer
// CounterResetHeader value used for integer histograms.
type floatHistogramAppendable interface {
	appendable(*histogram.FloatHistogram) (
		positiveInserts, negativeInserts,
		backwardPositiveInserts, backwardNegativeInserts []Insert,
		okToAppend, counterReset bool,
	)
}
