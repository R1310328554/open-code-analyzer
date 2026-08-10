// Copyright 2019 The Prometheus Authors
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

package index

// postingsstats 用固定大小 maxHeap 维护 Top-N 基数统计，供 MemPostings.Stats 输出 label/metric 高基数排行。

import (
	"math"
	"sort"
)

// Stat 记录名称与计数，用于 CardinalityMetricsStats 等排行列表。
// Stat holds values for a single cardinality statistic.
type Stat struct {
	Name  string
	Count uint64
}

// maxHeap 维护容量为 maxLength 的最小计数堆，push 时淘汰当前最小项。
type maxHeap struct {
	maxLength int
	minValue  uint64
	minIndex  int
	Items     []Stat
}

func (m *maxHeap) init(lenVal int) {
	m.maxLength = lenVal
	m.minValue = math.MaxUint64
	m.Items = make([]Stat, 0, lenVal)
}

func (m *maxHeap) push(item Stat) {
	if len(m.Items) < m.maxLength {
		if item.Count < m.minValue {
			m.minValue = item.Count
			m.minIndex = len(m.Items)
		}
		m.Items = append(m.Items, item)
		return
	}
	if item.Count < m.minValue {
		return
	}

	m.Items[m.minIndex] = item
	m.minValue = item.Count

	for i, stat := range m.Items {
		if stat.Count < m.minValue {
			m.minValue = stat.Count
			m.minIndex = i
		}
	}
}

// get 按 Count 降序排序后返回，供 Stats API 展示 Top 10 基数项。
func (m *maxHeap) get() []Stat {
	sort.Slice(m.Items, func(i, j int) bool {
		return m.Items[i].Count > m.Items[j].Count
	})
	return m.Items
}
// MemPostings.Stats 在 RLock 下遍历全部 label 对并填充四类基数统计。
