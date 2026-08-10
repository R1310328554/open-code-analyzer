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

// TSDB chunk 样本抽象：统一 float、原生 histogram 与 float histogram 的访问接口，供 compaction 与测试生成器使用。

package chunks

import (
	"github.com/prometheus/prometheus/model/histogram"
	"github.com/prometheus/prometheus/tsdb/chunkenc"
)

type Samples interface {
	Get(i int) Sample
	Len() int
}

type Sample interface {
	T() int64
	ST() int64
	F() float64
	H() *histogram.Histogram
	FH() *histogram.FloatHistogram
	Type() chunkenc.ValueType
	Copy() Sample // Returns a deep copy.
}

// SampleSlice 是 Samples 的切片实现。
type SampleSlice []Sample

func (s SampleSlice) Get(i int) Sample { return s[i] }
func (s SampleSlice) Len() int         { return len(s) }

type sample struct {
	st, t int64
	f     float64
	h     *histogram.Histogram
	fh    *histogram.FloatHistogram
}

func (s sample) T() int64 {
	return s.t
}

func (s sample) ST() int64 {
	return s.st
}

func (s sample) F() float64 {
	return s.f
}

func (s sample) H() *histogram.Histogram {
	return s.h
}

func (s sample) FH() *histogram.FloatHistogram {
	return s.fh
}

// Type 根据 h/fh 指针判断 histogram、float histogram 或 float。
func (s sample) Type() chunkenc.ValueType {
	switch {
	case s.h != nil:
		return chunkenc.ValHistogram
	case s.fh != nil:
		return chunkenc.ValFloatHistogram
	default:
		return chunkenc.ValFloat
	}
}

// Copy 深拷贝 histogram 字段，避免共享可变引用。
func (s sample) Copy() Sample {
	c := sample{t: s.t, f: s.f}
	if s.h != nil {
		c.h = s.h.Copy()
	}
	if s.fh != nil {
		c.fh = s.fh.Copy()
	}
	return c
}

// GenerateSamples 生成递增时间戳与 float 值的测试样本切片。
// GenerateSamples starting at start and counting up numSamples.
func GenerateSamples(start, numSamples int) []Sample {
	return generateSamples(start, numSamples, func(i int) Sample {
		return sample{
			t: int64(i),
			f: float64(i),
		}
	})
}

// generateSamples 通用样本生成器，由 gen 回调构造每个 Sample。
func generateSamples(start, numSamples int, gen func(int) Sample) []Sample {
	samples := make([]Sample, 0, numSamples)
	for i := start; i < start+numSamples; i++ {
		samples = append(samples, gen(i))
	}
	return samples
}
