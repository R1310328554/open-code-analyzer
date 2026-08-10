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

// postings 基数统计辅助：maxHeap 维护 Top-N 高基数 label/value 计数。

package index

import (
	"math"
	"slices"
)

// Stat 表示单个 label 或 value 的名称及其 series 计数。
// Stat holds values for a single cardinality statistic.
type Stat struct {
	Name  string
	Count uint64
}

type maxHeap struct {
	maxLength int
	minValue  uint64
	minIndex  int
	Items     []Stat
}

func (m *maxHeap) init(length int) {
	m.maxLength = length
	m.minValue = math.MaxUint64
	m.Items = make([]Stat, 0, length)
}

// push 若堆未满则追加，否则替换当前最小计数项。
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

// get 按 Count 降序返回堆中全部 Stat。
func (m *maxHeap) get() []Stat {
	slices.SortFunc(m.Items, func(a, b Stat) int {
		switch {
		case b.Count < a.Count:
			return -1
		case b.Count > a.Count:
			return 1
		default:
			return 0
		}
	})
	return m.Items
}
