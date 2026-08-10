//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
// metadata_utils.go — 文档元数据过滤引擎：解析 API 条件、支持 and/or 逻辑及多种比较运算符。

//

package common

import (
	"strconv"
	"strings"
)

// MetaCondition represents a single parsed filter condition.
// MetaCondition 单条元数据过滤条件。
type MetaCondition struct {
	// Operator 比较运算符（=、≠、>、contains、in 等）。
	Operator string      // "=", "≠", ">", "<", "≥", "≤", "contains", "not contains", "in", "not in", "start with", "end with", "empty", "not empty"
	// Key 元数据字段名。
	Key      string      // metadata field name
	// Value 比较右值，类型随运算符变化。
	Value    interface{} // comparison value
}

// MetaValueDocs maps a metadata field value to the document IDs that have that value.
// Example: {"Zhang San": ["doc1", "doc2"], "Li Si": ["doc3"]}
// MetaValueDocs 字段值到文档 ID 列表的映射。
type MetaValueDocs map[string][]string

// MetaData maps a metadata field name to its value→documents mapping.
// Example: {"author": {"Zhang San": ["doc1"]}, "year": {"2024": ["doc1", "doc2"]}}
// MetaData 全量元数据索引：字段名→值→文档集合。
type MetaData map[string]MetaValueDocs

// MetaFilterInput groups filter conditions with their logic operator.
// MetaFilterInput 过滤条件组及其 and/or 逻辑。
type MetaFilterInput struct {
	// Conditions 条件列表。
	Conditions []MetaCondition
	// Logic 多条件组合逻辑，默认 and。
	Logic      string // "and" | "or"
}

// operatorMapping translates Python-style operators to internal symbols.
// operatorMapping 将 Python/API 别名映射为内部运算符符号。
var operatorMapping = map[string]string{
	"is":     "=",
	"not is": "≠",
	">=":     "≥",
	"<=":     "≤",
	"!=":     "≠",
	"==":     "=",
}

// ParseAndConvert converts raw API conditions into MetaFilterInput.
// Equivalent to Python: meta_filter(metas, convert_conditions(cond), cond.get("logic"))
// ParseAndConvert 将 API 原始 conditions 转为 MetaFilterInput。
func ParseAndConvert(metadataCondition map[string]interface{}) *MetaFilterInput {
	if metadataCondition == nil {
		return nil
	}

	logic, _ := metadataCondition["logic"].(string)
	if logic == "" {
		logic = "and"
	}

	rawConditions, ok := metadataCondition["conditions"].([]interface{})
	if !ok || len(rawConditions) == 0 {
		return nil
	}

	var conditions []MetaCondition
	for _, raw := range rawConditions {
		cond, ok := raw.(map[string]interface{})
		if !ok {
			continue
		}
		name, _ := cond["name"].(string)
		if name == "" {
			name, _ = cond["key"].(string) // OpenAI API metadata_condition uses "key"
		}
		if name == "" {
			continue
		}
		op, _ := cond["comparison_operator"].(string)
		if op == "" {
			op, _ = cond["operator"].(string) // OpenAI API uses "operator"
		}
		op = convertOperator(op)
		conditions = append(conditions, MetaCondition{
			Operator: op,
			Key:      name,
			Value:    cond["value"],
		})
	}

	if len(conditions) == 0 {
		return nil
	}

	return &MetaFilterInput{
		Conditions: conditions,
		Logic:      logic,
	}
}

// convertOperator translates operator aliases to their canonical form.

// convertOperator 翻译运算符别名。
func convertOperator(op string) string {
	if mapped, exists := operatorMapping[op]; exists {
		return mapped
	}
	return op
}

// NormalizeOperator is the exported equivalent of convertOperator.
// NormalizeOperator 导出的运算符规范化入口。
func NormalizeOperator(op string) string { return convertOperator(op) }

// MetaFilter applies filter conditions against metadata and returns matching doc IDs.
// Python equivalent: common/metadata_utils.py::meta_filter()
// MetaFilter 对元数据索引应用过滤，返回匹配的文档 ID 列表。
func MetaFilter(metas MetaData, input *MetaFilterInput) []string {
	if input == nil || len(input.Conditions) == 0 {
		return nil
	}

	logic := input.Logic
	if logic == "" {
		logic = "and"
	}

	var docIDs *map[string]struct{}

	for _, f := range input.Conditions {
		v2docs, ok := metas[f.Key]
		if !ok {
			if logic == "and" {
				return []string{}
			}
			continue
		}

		matched := filterOut(v2docs, f.Operator, f.Value)

		if docIDs == nil {
			s := make(map[string]struct{}, len(matched))
			for _, id := range matched {
				s[id] = struct{}{}
			}
			docIDs = &s
		} else {
			if logic == "and" {
				s := make(map[string]struct{})
				for _, id := range matched {
					if _, exists := (*docIDs)[id]; exists {
						s[id] = struct{}{}
					}
				}
				docIDs = &s
				if len(*docIDs) == 0 {
					return []string{}
				}
			} else {
				for _, id := range matched {
					(*docIDs)[id] = struct{}{}
				}
			}
		}
	}

	if docIDs == nil {
		return []string{}
	}
	result := make([]string, 0, len(*docIDs))
	for id := range *docIDs {
		result = append(result, id)
	}
	return result
}

// filterOut returns matching doc IDs for a single (value → matchedDocs) map and operator.
// For "in" and "not in", it delegates to filterSet for O(n+m) hash-map-based filtering;
// all other operators use matchValue for per-element predicate evaluation.
// filterOut 对单个字段的值→文档映射执行运算符匹配。
func filterOut(v2docs MetaValueDocs, operator string, value interface{}) []string {
	if operator == "in" || operator == "not in" {
		return filterSet(v2docs, operator, value)
	}
	var ids []string
	for input, docids := range v2docs {
		if matchValue(input, operator, value) {
			ids = append(ids, docids...)
		}
	}
	return ids
}

// filterSet handles "in" and "not in" operators using O(1) hash map lookups.
//
// Instead of the O(n×m) linear scan that matchValue performs for these operators
// (n = distinct metadata values, m = filter list size), filterSet builds a lookup
// map from the filter value list once (O(m)) then tests each metadata entry in
// O(1) time (O(n)), yielding O(n+m) overall.
//
// Case sensitivity follows the same contract as matchValue:
//   - "in":      case-sensitive  (exact match via toString(item) == input)
//   - "not in":  case-insensitive (strings.ToLower on both sides)
//
// When value is not a []interface{} (should not happen in normal call paths),
// filterSet returns nil — no metadata values match "in", and for "not in" it
// defensively returns nil as well (rather than returning all entries, which could
// silently bypass a misconfigured filter).
// filterSet 用哈希表 O(n+m) 处理 in/not in 运算符。
func filterSet(v2docs MetaValueDocs, operator string, value interface{}) []string {
	list, ok := value.([]interface{})
	if !ok {
		return nil
	}

	if operator == "not in" {
		// Build case-insensitive exclusion set.
		lookup := make(map[string]bool, len(list))
		for _, item := range list {
			lookup[strings.ToLower(toString(item))] = true
		}
		var ids []string
		for input, docids := range v2docs {
			if !lookup[strings.ToLower(input)] {
				ids = append(ids, docids...)
			}
		}
		return ids
	}

	// "in": build case-sensitive inclusion set.
	lookup := make(map[string]bool, len(list))
	for _, item := range list {
		lookup[toString(item)] = true
	}
	var ids []string
	for input, docids := range v2docs {
		if lookup[input] {
			ids = append(ids, docids...)
		}
	}
	return ids
}

// matchValue checks if a single metadata value matches the operator+value.
// matchValue 判断单个元数据值是否满足运算符与右值。
func matchValue(input string, operator string, value interface{}) bool {
	switch operator {
	case "empty":
		return input == ""
	case "not empty":
		return input != ""
	}

	valStr := toString(value)

	switch operator {
	case "contains":
		return strings.Contains(strings.ToLower(input), strings.ToLower(valStr))
	case "not contains":
		return !strings.Contains(strings.ToLower(input), strings.ToLower(valStr))
	case "start with":
		return strings.HasPrefix(strings.ToLower(input), strings.ToLower(valStr))
	case "end with":
		return strings.HasSuffix(strings.ToLower(input), strings.ToLower(valStr))

		// "in" and "not in" are intentionally omitted from matchValue.
		// filterOut (line 177) intercepts these operators and delegates
		// them to filterSet for O(n+m) hash-map-based filtering, so they
		// never reach this function through normal call paths.
	}

	// Comparison operators: =, ≠, >, <, ≥, ≤
	return compareValues(input, valStr, operator)
}

// compareValues handles numeric/date/string comparison.
// compareValues 按日期/数值/字符串分支执行比较。
func compareValues(a, b, operator string) bool {
	// If filter value (b) is a date, only compare if data (a) is also a date.
	// Non-date values should not be compared against date filters (matching Python behavior).
	if isDate(b) {
		if !isDate(a) {
			return operator == "≠"
		}
		return compareString(a, b, operator)
	}

	// Try numeric comparison
	af, errA := strconv.ParseFloat(a, 64)
	bf, errB := strconv.ParseFloat(b, 64)
	if errA == nil && errB == nil {
		return compareFloat(af, bf, operator)
	}

	// Fall back to case-insensitive string comparison
	return compareString(strings.ToLower(a), strings.ToLower(b), operator)
}

// compareFloat 浮点数比较。
func compareFloat(a, b float64, operator string) bool {
	switch operator {
	case "=":
		return a == b
	case "≠":
		return a != b
	case ">":
		return a > b
	case "<":
		return a < b
	case "≥":
		return a >= b
	case "≤":
		return a <= b
	}
	return false
}

// compareString 字符串字典序比较。
func compareString(a, b string, operator string) bool {
	switch operator {
	case "=":
		return a == b
	case "≠":
		return a != b
	case ">":
		return a > b
	case "<":
		return a < b
	case "≥":
		return a >= b
	case "≤":
		return a <= b
	}
	return false
}

// isDate checks if a string is in YYYY-MM-DD format.
// isDate 判断字符串是否为 YYYY-MM-DD 日期格式。
func isDate(s string) bool {
	if len(s) != 10 {
		return false
	}
	if s[4] != '-' || s[7] != '-' {
		return false
	}
	for i := 0; i < 10; i++ {
		if i == 4 || i == 7 {
			continue
		}
		if s[i] < '0' || s[i] > '9' {
			return false
		}
	}
	return true
}

// toString converts a value to string for comparison.
// toString 将过滤右值转为可比较字符串。
func toString(v interface{}) string {
	if v == nil {
		return ""
	}
	switch s := v.(type) {
	case string:
		return s
	case float64:
		return strconv.FormatFloat(s, 'f', -1, 64)
	case bool:
		if s {
			return "true"
		}
		return "false"
	default:
		return ""
	}
}
