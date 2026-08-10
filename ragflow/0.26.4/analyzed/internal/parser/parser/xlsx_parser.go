//go:build cgo

//
// Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

// xlsx_parser — CGO 构建下 .xlsx 解析，excelize 保留行列结构。
package parser

import (
	"bytes"
	"fmt"
	"strings"

	"github.com/xuri/excelize/v2"
)

// XLSXParser 解析 Office Open XML 电子表格并输出 HTML。
type XLSXParser struct {
	libType string
}

// NewXLSXParser 目前仅支持 OfficeOxide 库类型。
func NewXLSXParser(libType string) (*XLSXParser, error) {
	switch libType {
	case OfficeOxide:
		return &XLSXParser{
			libType: OfficeOxide,
		}, nil
	default:
		return nil, fmt.Errorf("unsupported XLSX library type: %s", libType)
	}
}

func (p *XLSXParser) String() string {
	return "XLSXParser"
}

// ParseWithResult 将各工作表转为带 <h3> 标题的 HTML 表格，保留单元格边界。
func (p *XLSXParser) ParseWithResult(filename string, data []byte) ParseResult {
	f, err := excelize.OpenReader(bytes.NewReader(data))
	if err != nil {
		return ParseResult{Err: fmt.Errorf("xlsx open: %w", err)}
	}
	defer f.Close()

	sheets := f.GetSheetList()
	var html strings.Builder
	html.WriteString("<html><body>")
	for _, sheet := range sheets {
		html.WriteString("<h3>")
		html.WriteString(sheet)
		html.WriteString("</h3>")
		rows, err := f.GetRows(sheet)
		if err != nil {
			continue
		}
		html.WriteString("<table>")
		for _, row := range rows {
			html.WriteString("<tr>")
			for _, cell := range row {
				html.WriteString("<td>")
				html.WriteString(htmlEscape(cell))
				html.WriteString("</td>")
			}
			html.WriteString("</tr>")
		}
		html.WriteString("</table>")
	}
	html.WriteString("</body></html>")

	return ParseResult{
		OutputFormat: "html",
		File:         map[string]any{"name": filename, "format": "xlsx", "sheets": len(sheets)},
		HTML:         html.String(),
	}
}
// xlsx_parser.go — .xlsx 解析：excelize 读取工作表并输出 HTML，保留单元格边界。
