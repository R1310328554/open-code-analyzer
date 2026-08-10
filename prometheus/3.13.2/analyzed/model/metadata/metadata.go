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

// 指标元数据模型：存储 TYPE/UNIT/HELP，用于 exposition 解析、规则校验与 TSDB 元数据索引。

package metadata

import (
	"strings"

	"github.com/prometheus/common/model"
)

// Metadata 描述单条时间序列的类型、单位与帮助文本。
// Metadata stores a series' metadata information.
type Metadata struct {
	// Type 为 counter/gauge/histogram 等 Prometheus 指标类型。
	Type model.MetricType `json:"type"`
	// Unit 为 OpenMetrics 单位后缀（可为空）。
	Unit string           `json:"unit"`
	// Help 为人类可读的指标说明。
	Help string           `json:"help"`
}

// IsEmpty 在 type 为空/unknown 且 unit/help 皆空时返回 true。
// IsEmpty returns true if metadata structure is empty, including unknown type case.
func (m Metadata) IsEmpty() bool {
	return (m.Type == "" || m.Type == model.MetricTypeUnknown) && m.Unit == "" && m.Help == ""
}

// Equals 比较语义等价性，unknown 与空 type 视为相同。
// Equals returns true if m is semantically the same as other metadata.
func (m Metadata) Equals(other Metadata) bool {
	if strings.Compare(m.Unit, other.Unit) != 0 || strings.Compare(m.Help, other.Help) != 0 {
		return false
	}

// unknown 类型与空字符串 type 在相等性比较中互通。
	// Unknown means the same as empty string.
	if m.Type == "" || m.Type == model.MetricTypeUnknown {
		return other.Type == "" || other.Type == model.MetricTypeUnknown
	}
	return m.Type == other.Type
}
