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

// 将 PromQL 词法关键字、运算符、聚合器与函数注册到util/features 特性开关，供运行时查询语言能力边界。

package parser

import "github.com/prometheus/prometheus/util/features"

// RegisterFeatures 按 parser 选项启用/禁用实验性与扩展语法特性。
// RegisterFeatures registers all PromQL features with the feature registry.
// This includes operators (arithmetic and comparison/set), aggregators (standard
// and experimental), and functions.
func (pql *promQLParser) RegisterFeatures(r features.Collector) {
	// Register core PromQL language keywords.
	for keyword, itemType := range key {
		if itemType.IsKeyword() {
			switch keyword {
			case "anchored", "smoothed":
				r.Set(features.PromQL, keyword, pql.options.EnableExtendedRangeSelectors)
			case "fill", "fill_left", "fill_right":
				r.Set(features.PromQL, keyword, pql.options.EnableBinopFillModifiers)
			default:
				r.Enable(features.PromQL, keyword)
			}
		}
	}

// 遍历 ItemType 运算符区间，向特性收集器登记 PromQLOperators。
	// Register operators.
	for o := ItemType(operatorsStart + 1); o < operatorsEnd; o++ {
		if o.IsOperator() {
			r.Set(features.PromQLOperators, o.String(), true)
		}
	}

	// Register aggregators.
	for a := ItemType(aggregatorsStart + 1); a < aggregatorsEnd; a++ {
		if a.IsAggregator() {
			experimental := a.IsExperimentalAggregator() && !pql.options.EnableExperimentalFunctions
			r.Set(features.PromQLOperators, a.String(), !experimental)
		}
	}

// 按 Functions 表注册各 PromQL 函数，实验函数受 EnableExperimentalFunctions 控制。
	// Register functions.
	for f, fc := range Functions {
		r.Set(features.PromQLFunctions, f, !fc.Experimental || pql.options.EnableExperimentalFunctions)
	}

	// Register experimental parser features.
	r.Set(features.PromQL, "duration_expr", pql.options.ExperimentalDurationExpr)
}
