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
// table.go — CLI 表格输出：支持 table/plain/json 三种格式的列对齐与 CJK 宽度计算。

//

package cli

import (
	"encoding/json"
	"fmt"
	"strconv"
	"strings"
)

// maxColWidth 限制单列最大显示宽度，防止终端被撑爆。
const maxColWidth = 256

// PrintTableSimple prints data in a simple table format (default: table format with borders)
// Similar to Python's _print_table_simple
// PrintTableSimple 默认以 table 格式打印键值行数据。
func PrintTableSimple(data []map[string]interface{}) {
	PrintTableSimpleByFormat(data, OutputFormatTable)
}

// PrintTableSimpleByFormat prints data in the specified format
// Supports: table (with borders), plain (no borders, space-separated), json
// - Column names in lowercase
// - Two spaces between columns
// - Numeric columns right-aligned
// - URI/path columns not truncated
// PrintTableSimpleByFormat 按 format 选择边框表格、纯文本或 JSON。
func PrintTableSimpleByFormat(data []map[string]interface{}, format OutputFormat) {
	if len(data) == 0 {
		if format == OutputFormatJSON {
			fmt.Println("[]")
		} else if format == OutputFormatPlain {
			fmt.Println("(empty)")
		} else {
			fmt.Println("No data to print")
		}
		return
	}

	// JSON format: output as JSON array
	if format == OutputFormatJSON {
		jsonData, err := json.MarshalIndent(data, "", "  ")
		if err != nil {
			fmt.Printf("Error marshaling JSON: %v\n", err)
			return
		}
		fmt.Println(string(jsonData))
		return
	}

	// Collect all column names
	columnSet := make(map[string]bool)
	for _, item := range data {
		for key := range item {
			columnSet[key] = true
		}
	}

	// Sort columns
	columns := make([]string, 0, len(columnSet))
	for col := range columnSet {
		columns = append(columns, col)
	}
	// Simple sort - in production you might want specific column ordering
	for i := 0; i < len(columns); i++ {
		for j := i + 1; j < len(columns); j++ {
			if columns[i] > columns[j] {
				columns[i], columns[j] = columns[j], columns[i]
			}
		}
	}

	// Analyze columns: check if numeric and if URI column
	colIsNumeric := make(map[string]bool)
	colIsURI := make(map[string]bool)
	for _, col := range columns {
		colLower := strings.ToLower(col)
		if colLower == "uri" || colLower == "path" || colLower == "id" {
			colIsURI[col] = true
		}
		// Check if all values are numeric
		isNumeric := true
		for _, item := range data {
			if val, ok := item[col]; ok {
				if !isNumericValue(val) {
					isNumeric = false
					break
				}
			}
		}
		colIsNumeric[col] = isNumeric
	}

	// Calculate column widths (capped at maxColWidth)
	colWidths := make(map[string]int)
	for _, col := range columns {
		maxWidth := getStringWidth(strings.ToLower(col))
		for _, item := range data {
			value := formatValue(item[col])
			valueWidth := getStringWidth(value)
			if valueWidth > maxWidth {
				maxWidth = valueWidth
			}
		}
		if maxWidth > maxColWidth {
			maxWidth = maxColWidth
		}
		if maxWidth < 2 {
			maxWidth = 2
		}
		colWidths[col] = maxWidth
	}

	if format == OutputFormatPlain {
		// Plain mode: no borders, space-separated (ov CLI compatible)
		// Print header (lowercase column names, right-aligned for numeric columns)
		headerParts := make([]string, 0, len(columns))
		for _, col := range columns {
			// Header follows the same alignment as data (right-aligned for numeric columns)
			headerParts = append(headerParts, padCell(strings.ToLower(col), colWidths[col], colIsNumeric[col]))
		}
		fmt.Println(strings.Join(headerParts, "  "))

		// Print data rows
		for _, item := range data {
			rowParts := make([]string, 0, len(columns))
			for _, col := range columns {
				value := formatValue(item[col])
				isURI := colIsURI[col]
				isNumeric := colIsNumeric[col]

				// URI columns: never truncate, no padding if too long
				if isURI && getStringWidth(value) > colWidths[col] {
					rowParts = append(rowParts, value)
				} else {
					// Normal cell: truncate if too long, then pad
					valueWidth := getStringWidth(value)
					if valueWidth > colWidths[col] {
						runes := []rune(value)
						value = truncateStringByWidth(runes, colWidths[col])
						valueWidth = getStringWidth(value)
					}
					rowParts = append(rowParts, padCell(value, colWidths[col], isNumeric))
				}
			}
			fmt.Println(strings.Join(rowParts, "  "))
		}
	} else {
		// Normal mode: with borders
		// Generate separator
		separatorParts := make([]string, 0, len(columns))
		for _, col := range columns {
			separatorParts = append(separatorParts, strings.Repeat("-", colWidths[col]+2))
		}
		separator := "+" + strings.Join(separatorParts, "+") + "+"

		// Print header
		fmt.Println(separator)
		headerParts := make([]string, 0, len(columns))
		for _, col := range columns {
			headerParts = append(headerParts, fmt.Sprintf(" %-*s ", colWidths[col], col))
		}
		fmt.Println("|" + strings.Join(headerParts, "|") + "|")
		fmt.Println(separator)

		// Print data rows
		for _, item := range data {
			rowParts := make([]string, 0, len(columns))
			for _, col := range columns {
				value := formatValue(item[col])
				valueWidth := getStringWidth(value)
				// Truncate if too long
				if valueWidth > colWidths[col] {
					runes := []rune(value)
					value = truncateStringByWidth(runes, colWidths[col])
					valueWidth = getStringWidth(value)
				}
				// Pad to column width
				rowParts = append(rowParts, fmt.Sprintf(" %s ", padCell(value, colWidths[col], false)))
			}
			fmt.Println("|" + strings.Join(rowParts, "|") + "|")
		}

		fmt.Println(separator)
	}
}

// formatValue formats a value for display
// formatValue 将 interface{} 格式化为字符串用于单元格显示。
func formatValue(v interface{}) string {
	if v == nil {
		return ""
	}
	switch val := v.(type) {
	case string:
		return val
	case int:
		return strconv.Itoa(val)
	case int64:
		return strconv.FormatInt(val, 10)
	case float64:
		return strconv.FormatFloat(val, 'f', -1, 64)
	case bool:
		return strconv.FormatBool(val)
	default:
		return fmt.Sprintf("%v", v)
	}
}

// isNumericValue checks if a value is numeric
// isNumericValue 判断列值是否全为数字以决定右对齐。
func isNumericValue(v interface{}) bool {
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

// truncateStringByWidth truncates a string to fit within maxWidth display width
// truncateStringByWidth 按显示宽度截断并追加省略号。
func truncateStringByWidth(runes []rune, maxWidth int) string {
	width := 0
	for i, r := range runes {
		if isHalfWidth(r) {
			width++
		} else {
			width += 2
		}
		if width > maxWidth-3 {
			return string(runes[:i]) + "..."
		}
	}
	return string(runes)
}

// padCell pads a string to the specified width for alignment
// padCell 按宽度与对齐方式填充单元格内容。
func padCell(content string, width int, alignRight bool) string {
	contentWidth := getStringWidth(content)
	if contentWidth >= width {
		return content
	}
	padding := width - contentWidth
	if alignRight {
		return strings.Repeat(" ", padding) + content
	}
	return content + strings.Repeat(" ", padding)
}

// getStringWidth calculates the display width of a string
// Treats CJK characters as width 2
// getStringWidth 计算字符串终端显示宽度（CJK 计为 2）。
func getStringWidth(text string) int {
	width := 0
	for _, r := range text {
		if isHalfWidth(r) {
			width++
		} else {
			width += 2
		}
	}
	return width
}

// isHalfWidth checks if a rune is half-width
// isHalfWidth 判断 rune 是否为半角 ASCII 可打印字符。
func isHalfWidth(r rune) bool {
	// ASCII printable characters and common whitespace
	if r >= 0x20 && r <= 0x7E {
		return true
	}
	if r == '\t' || r == '\n' || r == '\r' {
		return true
	}
	return false
}
