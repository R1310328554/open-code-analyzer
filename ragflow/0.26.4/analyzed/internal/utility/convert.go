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
//

package utility

// convert.go 提供 JSON/时间/十六进制位置等通用转换与序列化辅助。

import (
	"encoding/json"
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"
)

// JSONFloat64 序列化时始终带小数点（如 0.0 而非 0）。
type JSONFloat64 float64

func (f JSONFloat64) MarshalJSON() ([]byte, error) {
	// Always output with decimal point (e.g., 0.0 instead of 0)
	return []byte(fmt.Sprintf("%.1f", float64(f))), nil
}

// GetProjectBaseDirectory 返回当前工作目录，失败时返回 "."。
//
// Returns:
//   - string: The current working directory path, or "." if an error occurs.
//
// Example:
//
//	baseDir := utility.GetProjectBaseDirectory()
//	configPath := filepath.Join(baseDir, "conf", "config.json")
func GetProjectBaseDirectory() string {
	cwd, err := os.Getwd()
	if err != nil {
		return "."
	}
	return cwd
}

// StringPtr 非空字符串转 *string，空串返回 nil。
//
// Parameters:
//   - s: The string to convert to a pointer.
//
// Returns:
//   - *string: A pointer to the input string, or nil if the input is empty.
//
// Example:
//
//	name := utility.StringPtr("example")  // returns &"example"
//	empty := utility.StringPtr("")        // returns nil
func StringPtr(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}

// ParseInt64 解析 int64，失败返回 0。
//
// Parameters:
//   - s: The string to parse.
//
// Returns:
//   - int64: The parsed integer value, or 0 if parsing fails.
//
// Example:
//
//	val := utility.ParseInt64("123")   // returns 123
//	val := utility.ParseInt64("abc")   // returns 0
//	val := utility.ParseInt64("")      // returns 0
func ParseInt64(s string) int64 {
	var result int64
	fmt.Sscanf(s, "%d", &result)
	return result
}

// FormatTime 格式化时间为展示字符串；零值显示 Perpetual。
func FormatTime(t time.Time) string {
	if t.IsZero() {
		return "N/A (Perpetual)"
	}
	return t.Format("2006-01-02 15:04:05")
}

// FormatTimeToString 按指定 layout 格式化 *time.Time，nil 返回 nil。
func FormatTimeToString(t *time.Time, format string) interface{} {
	if t == nil {
		return nil
	}
	return t.Format(format)
}

// ConvertHexToPositionIntArray 十六进制串转位置 int 二维数组（每 5 个一组）。
func ConvertHexToPositionIntArray(hexStr string) interface{} {
	if hexStr == "" {
		return nil
	}

	parts := strings.Split(hexStr, "_")
	var intVals []int
	for _, part := range parts {
		if part == "" {
			continue
		}
		val, err := strconv.ParseInt(part, 16, 64)
		if err != nil {
			continue
		}
		intVals = append(intVals, int(val))
	}

	if len(intVals) == 0 {
		return nil
	}

	// 每 5 个整数为一组位置坐标
	var result [][]int
	for i := 0; i < len(intVals); i += 5 {
		end := i + 5
		if end > len(intVals) {
			end = len(intVals)
		}
		result = append(result, intVals[i:end])
	}

	return result
}

// ConvertPositionIntArrayToHex 二维 position_int 转固定宽度十六进制串。
func ConvertPositionIntArrayToHex(list []interface{}) string {
	var hexParts []string
	for _, item := range list {
		if inner, ok := item.([]interface{}); ok {
			for _, num := range inner {
				if n, ok := num.(float64); ok {
					hexParts = append(hexParts, fmt.Sprintf("%08x", int64(n)))
				} else if n, ok := num.(int64); ok {
					hexParts = append(hexParts, fmt.Sprintf("%08x", n))
				} else if n, ok := num.(int); ok {
					hexParts = append(hexParts, fmt.Sprintf("%08x", n))
				}
			}
		}
	}
	return strings.Join(hexParts, "_")
}

// ConvertHexToIntArray 下划线分隔十六进制段转 []int。
func ConvertHexToIntArray(hexStr string) interface{} {
	if hexStr == "" {
		return nil
	}

	parts := strings.Split(hexStr, "_")
	var result []int
	for _, part := range parts {
		if part == "" {
			continue
		}
		val, err := strconv.ParseInt(part, 16, 64)
		if err != nil {
			continue
		}
		result = append(result, int(val))
	}

	if len(result) == 0 {
		return nil
	}
	return result
}

// ConvertIntArrayToHex int 数组转 8 位十六进制段拼接串。
func ConvertIntArrayToHex(list []interface{}) string {
	var hexParts []string
	for _, num := range list {
		if n, ok := num.(float64); ok {
			hexParts = append(hexParts, fmt.Sprintf("%08x", int64(n)))
		} else if n, ok := num.(int64); ok {
			hexParts = append(hexParts, fmt.Sprintf("%08x", n))
		} else if n, ok := num.(int); ok {
			hexParts = append(hexParts, fmt.Sprintf("%08x", n))
		}
	}
	return strings.Join(hexParts, "_")
}

// IsEmpty 判断 nil、空切片或空字符串。
func IsEmpty(v interface{}) bool {
	if v == nil {
		return true
	}
	if arr, ok := v.([]interface{}); ok {
		return len(arr) == 0
	}
	if arr, ok := v.([]string); ok {
		return len(arr) == 0
	}
	if arr, ok := v.([]int); ok {
		return len(arr) == 0
	}
	if strVal, ok := v.(string); ok && strVal == "" {
		return true
	}
	return false
}

// IsNumericValue 判断值是否为数值类型或可解析数值字符串。
func IsNumericValue(v interface{}) bool {
	if v == nil {
		return false
	}
	switch val := v.(type) {
	case int, int8, int16, int32, int64:
		return true
	case uint, uint8, uint16, uint32, uint64:
		return true
	case float32, float64:
		return true
	case string:
		_, err := strconv.ParseFloat(val, 64)
		return err == nil
	default:
		return false
	}
}

// SetFieldArray 写入 map 字段；空值写 []interface{}{}。
func SetFieldArray(result map[string]interface{}, destKey string, v interface{}) {
	if IsEmpty(v) {
		result[destKey] = []interface{}{}
	} else {
		result[destKey] = v
	}
}

// ToFloat64 将多种类型转为 float64。
func ToFloat64(val interface{}) (float64, bool) {
	switch v := val.(type) {
	case float64:
		return v, true
	case float32:
		return float64(v), true
	case int:
		return float64(v), true
	case int64:
		return float64(v), true
	case string:
		f, err := strconv.ParseFloat(v, 64)
		if err != nil {
			return 0, false
		}
		return f, true
	default:
		return 0, false
	}
}

// ConvertToStringSlice 将 interface{} 转为 []string。
// e.g. []interface{}{"a", "b", "c"} -> []string{"a", "b", "c"}
// e.g. "hello" -> []string{"hello"}
func ConvertToStringSlice(v interface{}) []string {
	if v == nil {
		return nil
	}
	switch val := v.(type) {
	case []interface{}:
		result := make([]string, 0, len(val))
		for _, item := range val {
			if s, ok := item.(string); ok {
				result = append(result, s)
			} else {
				result = append(result, fmt.Sprintf("%v", item))
			}
		}
		return result
	case []string:
		return val
	case string:
		return []string{val}
	default:
		return nil
	}
}

// ConvertToString 将值转为空格拼接字符串。
// For []interface{}, joins elements with space; for other types, returns string representation
// e.g. []interface{}{"a", "b", "c"} -> "a b c"
// e.g. "hello" -> "hello"
func ConvertToString(v interface{}) string {
	if v == nil {
		return ""
	}
	switch val := v.(type) {
	case []interface{}:
		parts := make([]string, 0, len(val))
		for _, item := range val {
			if s, ok := item.(string); ok {
				parts = append(parts, s)
			} else {
				parts = append(parts, fmt.Sprintf("%v", item))
			}
		}
		return strings.Join(parts, " ")
	default:
		return fmt.Sprintf("%v", v)
	}
}

// ConvertMapToJSONString map 转 JSON 字符串供 Infinity JSON 列存储；nil 为 "{}"。
//
// e.g. map[string]interface{}{"key": "value"}) -> `"{\"key\":\"value\"}"`
func ConvertMapToJSONString(v interface{}) interface{} {
	if v == nil {
		return "{}"
	}
	if m, ok := v.(map[string]interface{}); ok {
		jsonBytes, _ := json.Marshal(m)
		return string(jsonBytes)
	}
	return v
}

// FloatToString 按 Python str() 风格格式化浮点（整数补 .0）。
func FloatToString(f float64) string {
	s := strconv.FormatFloat(f, 'f', -1, 64)
	if !strings.Contains(s, ".") && !strings.Contains(s, "e") {
		s = s + ".0"
	}
	return s
}
// convert.go — 通用类型转换、十六进制位置编码与 JSON 序列化辅助。
