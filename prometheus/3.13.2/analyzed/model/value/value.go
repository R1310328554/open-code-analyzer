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

// Prometheus 特殊 float64 位模式：NormalNaN 表示缺失样本，StaleNaN 标记 series 过期（stale marker），供 TSDB 与 scrape 使用。

package value

import (
	"math"
)

const (
// NormalNaN 为 quiet NaN 位模式，等价于 math.NaN()。
	// NormalNaN is a quiet NaN. This is also math.NaN().
	NormalNaN uint64 = 0x7ff8000000000001

// StaleNaN 为 signaling NaN，用于标记 stale 样本以便下游识别。
	// StaleNaN is a signaling NaN, due to the MSB of the mantissa being 0.
	// This value is chosen with many leading 0s, so we have scope to store more
	// complicated values in the future. It is 2 rather than 1 to make
	// it easier to distinguish from the NormalNaN by a human when debugging.
	StaleNaN uint64 = 0x7ff0000000000002
)

// IsStaleNaN 比较 float64 位模式是否为 StaleNaN 常量。
// IsStaleNaN returns true when the provided NaN value is a stale marker.
func IsStaleNaN(v float64) bool {
	return math.Float64bits(v) == StaleNaN
}
