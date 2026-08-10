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

// prompb v1 远程读写 protobuf 与 Prometheus 内存模型的双向转换：标签、元数据类型、直方图（整型/浮点）及 exemplar。

package prompb

import (
	"strings"

	"github.com/prometheus/common/model"

	"github.com/prometheus/prometheus/model/exemplar"
	"github.com/prometheus/prometheus/model/histogram"
	"github.com/prometheus/prometheus/model/labels"
)

// 转换逻辑在 prompb/rwcommon 包中有对应测试覆盖。
// NOTE(bwplotka): This file's code is tested in /prompb/rwcommon.

// ToLabels 将远程 TimeSeries/ChunkedSeries 的 Label 对转为排序后的 labels.Labels。
// ToLabels return model labels.Labels from timeseries' remote labels.
func (m TimeSeries) ToLabels(b *labels.ScratchBuilder, _ []string) labels.Labels {
	return labelProtosToLabels(b, m.GetLabels())
}

// ToLabels return model labels.Labels from timeseries' remote labels.
func (m ChunkedSeries) ToLabels(b *labels.ScratchBuilder, _ []string) labels.Labels {
	return labelProtosToLabels(b, m.GetLabels())
}

// labelProtosToLabels 复用 ScratchBuilder 组装并排序标签。
func labelProtosToLabels(b *labels.ScratchBuilder, labelPairs []Label) labels.Labels {
	b.Reset()
	for _, l := range labelPairs {
		b.Add(l.Name, l.Value)
	}
	b.Sort()
	return b.Labels()
}

// FromLabels 将 labels.Labels 写入可复用的 []Label 缓冲，减少分配。
// FromLabels transforms labels into prompb labels. The buffer slice
// will be used to avoid allocations if it is big enough to store the labels.
func FromLabels(lbls labels.Labels, buf []Label) []Label {
	result := buf[:0]
	lbls.Range(func(l labels.Label) {
		result = append(result, Label{
			Name:  l.Name,
			Value: l.Value,
		})
	})
	return result
}

// FromMetadataType 将 model.MetricType 字符串映射为 MetricMetadata 枚举。
// FromMetadataType transforms a Prometheus metricType into prompb metricType. Since the former is a string we need to transform it to an enum.
func FromMetadataType(t model.MetricType) MetricMetadata_MetricType {
	mt := strings.ToUpper(string(t))
	v, ok := MetricMetadata_MetricType_value[mt]
	if !ok {
		return MetricMetadata_UNKNOWN
	}
	return MetricMetadata_MetricType(v)
}

// IsFloatHistogram 通过 oneof Count 分支判断是否为浮点直方图。
// IsFloatHistogram returns true if the histogram is float.
func (h Histogram) IsFloatHistogram() bool {
	_, ok := h.GetCount().(*Histogram_CountFloat)
	return ok
}

// ToIntHistogram 从远程整型直方图还原 histogram.Histogram；浮点型返回 nil。
// ToIntHistogram returns integer Prometheus histogram from the remote implementation
// of integer histogram. If it's a float histogram, the method returns nil.
func (h Histogram) ToIntHistogram() *histogram.Histogram {
	if h.IsFloatHistogram() {
		return nil
	}
	return &histogram.Histogram{
		CounterResetHint: histogram.CounterResetHint(h.ResetHint),
		Schema:           h.Schema,
		ZeroThreshold:    h.ZeroThreshold,
		ZeroCount:        h.GetZeroCountInt(),
		Count:            h.GetCountInt(),
		Sum:              h.Sum,
		PositiveSpans:    spansProtoToSpans(h.GetPositiveSpans()),
		PositiveBuckets:  h.GetPositiveDeltas(),
		NegativeSpans:    spansProtoToSpans(h.GetNegativeSpans()),
		NegativeBuckets:  h.GetNegativeDeltas(),
		CustomValues:     h.CustomValues,
	}
}

// ToFloatHistogram 还原或转换得到 histogram.FloatHistogram。
// ToFloatHistogram returns float Prometheus histogram from the remote implementation
// of float histogram. If the underlying implementation is an integer histogram, a
// conversion is performed.
func (h Histogram) ToFloatHistogram() *histogram.FloatHistogram {
	if h.IsFloatHistogram() {
		return &histogram.FloatHistogram{
			CounterResetHint: histogram.CounterResetHint(h.ResetHint),
			Schema:           h.Schema,
			ZeroThreshold:    h.ZeroThreshold,
			ZeroCount:        h.GetZeroCountFloat(),
			Count:            h.GetCountFloat(),
			Sum:              h.Sum,
			PositiveSpans:    spansProtoToSpans(h.GetPositiveSpans()),
			PositiveBuckets:  h.GetPositiveCounts(),
			NegativeSpans:    spansProtoToSpans(h.GetNegativeSpans()),
			NegativeBuckets:  h.GetNegativeCounts(),
			CustomValues:     h.CustomValues, // CustomValues are immutable.
		}
	}
	// Conversion from integer histogram.
	return &histogram.FloatHistogram{
		CounterResetHint: histogram.CounterResetHint(h.ResetHint),
		Schema:           h.Schema,
		ZeroThreshold:    h.ZeroThreshold,
		ZeroCount:        float64(h.GetZeroCountInt()),
		Count:            float64(h.GetCountInt()),
		Sum:              h.Sum,
		PositiveSpans:    spansProtoToSpans(h.GetPositiveSpans()),
		PositiveBuckets:  deltasToCounts(h.GetPositiveDeltas()),
		NegativeSpans:    spansProtoToSpans(h.GetNegativeSpans()),
		NegativeBuckets:  deltasToCounts(h.GetNegativeDeltas()),
		CustomValues:     h.CustomValues, // CustomValues are immutable.
	}
}

func spansProtoToSpans(s []BucketSpan) []histogram.Span {
	spans := make([]histogram.Span, len(s))
	for i := range s {
		spans[i] = histogram.Span{Offset: s[i].Offset, Length: s[i].Length}
	}

	return spans
}

// deltasToCounts 将 delta 编码桶累积为绝对 count 序列。
func deltasToCounts(deltas []int64) []float64 {
	counts := make([]float64, len(deltas))
	var cur float64
	for i, d := range deltas {
		cur += float64(d)
		counts[i] = cur
	}
	return counts
}

// FromIntHistogram 将内存整型直方图编码为远程写入用的 Histogram 消息。
// FromIntHistogram returns remote Histogram from the integer Histogram.
func FromIntHistogram(timestamp int64, h *histogram.Histogram) Histogram {
	return Histogram{
		Count:          &Histogram_CountInt{CountInt: h.Count},
		Sum:            h.Sum,
		Schema:         h.Schema,
		ZeroThreshold:  h.ZeroThreshold,
		ZeroCount:      &Histogram_ZeroCountInt{ZeroCountInt: h.ZeroCount},
		NegativeSpans:  spansToSpansProto(h.NegativeSpans),
		NegativeDeltas: h.NegativeBuckets,
		PositiveSpans:  spansToSpansProto(h.PositiveSpans),
		PositiveDeltas: h.PositiveBuckets,
		ResetHint:      Histogram_ResetHint(h.CounterResetHint),
		Timestamp:      timestamp,
		CustomValues:   h.CustomValues, // CustomValues are immutable.
	}
}

// FromFloatHistogram 将 FloatHistogram 编码为远程 protobuf 直方图。
// FromFloatHistogram returns remote Histogram from the float Histogram.
func FromFloatHistogram(timestamp int64, fh *histogram.FloatHistogram) Histogram {
	return Histogram{
		Count:          &Histogram_CountFloat{CountFloat: fh.Count},
		Sum:            fh.Sum,
		Schema:         fh.Schema,
		ZeroThreshold:  fh.ZeroThreshold,
		ZeroCount:      &Histogram_ZeroCountFloat{ZeroCountFloat: fh.ZeroCount},
		NegativeSpans:  spansToSpansProto(fh.NegativeSpans),
		NegativeCounts: fh.NegativeBuckets,
		PositiveSpans:  spansToSpansProto(fh.PositiveSpans),
		PositiveCounts: fh.PositiveBuckets,
		ResetHint:      Histogram_ResetHint(fh.CounterResetHint),
		Timestamp:      timestamp,
		CustomValues:   fh.CustomValues, // CustomValues are immutable.
	}
}

func spansToSpansProto(s []histogram.Span) []BucketSpan {
	spans := make([]BucketSpan, len(s))
	for i := range s {
		spans[i] = BucketSpan{Offset: s[i].Offset, Length: s[i].Length}
	}

	return spans
}

// ToExemplar 解析远程 Exemplar 标签与值，填充 exemplar.Exemplar。
// ToExemplar converts remote exemplar to model exemplar.
func (m Exemplar) ToExemplar(b *labels.ScratchBuilder, _ []string) exemplar.Exemplar {
	timestamp := m.Timestamp

	return exemplar.Exemplar{
		Labels: labelProtosToLabels(b, m.GetLabels()),
		Value:  m.Value,
		Ts:     timestamp,
		HasTs:  timestamp != 0,
	}
}
