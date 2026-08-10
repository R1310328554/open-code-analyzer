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

// 标签与 UI 链接工具：生成 Graph/Table 表达式 URL，以及 Prometheus 标签名清洗规则。

package strutil

import (
	"fmt"
	"net/url"
	"strings"

	"github.com/grafana/regexp"
)

var invalidLabelCharRE = regexp.MustCompile(`[^a-zA-Z0-9_]`)

// TableLinkForExpression 生成指向表格视图的 /graph 相对链接（g0.tab=1）。
// TableLinkForExpression creates an escaped relative link to the table view of
// the provided expression.
func TableLinkForExpression(expr string) string {
	escapedExpression := url.QueryEscape(expr)
	return fmt.Sprintf("/graph?g0.expr=%s&g0.tab=1", escapedExpression)
}

// GraphLinkForExpression 生成指向图形视图的 /graph 相对链接（g0.tab=0）。
// GraphLinkForExpression creates an escaped relative link to the graph view of
// the provided expression.
func GraphLinkForExpression(expr string) string {
	escapedExpression := url.QueryEscape(expr)
	return fmt.Sprintf("/graph?g0.expr=%s&g0.tab=0", escapedExpression)
}

// SanitizeLabelName 将非法字符替换为下划线（不处理数字开头等完整规则）。
// SanitizeLabelName replaces anything that doesn't match
// client_label.LabelNameRE with an underscore.
// Note: this does not handle all Prometheus label name restrictions (such as
// not starting with a digit 0-9), and hence should only be used if the label
// name is prefixed with a known valid string.
func SanitizeLabelName(name string) string {
	return invalidLabelCharRE.ReplaceAllString(name, "_")
}

// SanitizeFullLabelName 完整清洗标签名，空串返回单个下划线。
// SanitizeFullLabelName replaces any invalid character with an underscore, and
// if given an empty string, returns a string containing a single underscore.
func SanitizeFullLabelName(name string) string {
	if name == "" {
		return "_"
	}
	var validSb strings.Builder
	for i, b := range name {
		if (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || b == '_' || (b >= '0' && b <= '9' && i > 0) {
			validSb.WriteRune(b)
		} else {
			validSb.WriteRune('_')
		}
	}
	return validSb.String()
}
