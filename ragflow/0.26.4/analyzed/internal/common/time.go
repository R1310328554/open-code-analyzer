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
// time.go — ISO 8601 时间解析与格式化：兼容多种 RFC3339 变体，对齐 Python dateutil.isoparse 行为。

//

package common

import (
	"log/slog"
	"strings"
	"time"
)

// ParseISO8601 依次尝试 RFC3339Nano、RFC3339、无时区与纯日期格式；Z 后缀自动归一化为 +00:00（PR #16483）。
func ParseISO8601(dateString string) (time.Time, error) {
	// 将 Z 后缀替换为 +00:00 以兼容 time.RFC3339 解析。
	normalized := dateString
	if strings.HasSuffix(dateString, "Z") {
		normalized = dateString[:len(dateString)-1] + "+00:00"
	}

	layouts := []string{
		time.RFC3339Nano,      // "2006-01-02T15:04:05.999999999Z07:00"
		time.RFC3339,          // "2006-01-02T15:04:05Z07:00"
		"2006-01-02T15:04:05", // no timezone
		"2006-01-02",          // date only
	}
	for _, layout := range layouts {
		var t time.Time
		var err error
		if strings.Contains(layout, "Z07:00") || strings.Contains(layout, "MST") {
			t, err = time.Parse(layout, normalized)
		} else {
			t, err = time.ParseInLocation(layout, normalized, time.Local)
		}
		if err == nil {
			return t, nil
		}
	}
	return time.Time{}, &time.ParseError{
		Layout:     "ISO 8601",
		Value:      dateString,
		LayoutElem: "",
		ValueElem:  dateString,
		Message:    "failed to parse as any supported ISO 8601 variant",
	}
}

// FormatISO8601ToYMDHMS 解析 ISO 8601 并格式化为 "YYYY-MM-DD HH:MM:SS"；解析失败时原样返回，对齐 Python time_utils.py。
func FormatISO8601ToYMDHMS(timeStr string) string {
	dt, err := ParseISO8601(timeStr)
	if err != nil {
		slog.Error("FormatISO8601ToYMDHMS parse error", "input", timeStr, "error", err)
		return timeStr
	}
	return dt.Format("2006-01-02 15:04:05")
}

// DeltaSeconds 计算给定时间字符串距现在的秒数；支持 ISO 8601 与 "YYYY-MM-DD HH:MM:SS" 两种格式。
func DeltaSeconds(dateString string) (float64, error) {
	// 优先用 ParseISO8601 灵活解析 ISO 8601 / RFC3339。
	dt, err := ParseISO8601(dateString)
	if err == nil {
		return time.Since(dt).Seconds(), nil
	}

	// 回退到本地时区的 "2006-01-02 15:04:05" 格式。
	const layout = "2006-01-02 15:04:05"
	dt, err = time.ParseInLocation(layout, dateString, time.Local)
	if err != nil {
		return 0, err
	}
	return time.Since(dt).Seconds(), nil
}
