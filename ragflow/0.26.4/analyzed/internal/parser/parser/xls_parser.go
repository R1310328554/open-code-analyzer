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

// xls_parser — CGO 构建下旧版 Excel（.xls）解析，统一走 excelize。
package parser

import (
	"bytes"
	"fmt"
	"strings"

	"github.com/xuri/excelize/v2"
)

// XLSParser 解析 .xls 二进制表格，输出 HTML 供下游分块。
type XLSParser struct {
	libType string
}

// NewXLSParser 目前仅支持 OfficeOxide 库类型。
func NewXLSParser(libType string) (*XLSParser, error) {
	switch libType {
	case OfficeOxide:
		return &XLSParser{
			libType: OfficeOxide,
		}, nil
	default:
		return nil, fmt.Errorf("unsupported XLS library type: %s", libType)
	}
}

func (p *XLSParser) String() string {
	return "XLSParser"
}

// ParseWithResult 经 excelize 打开工作簿，各 sheet 渲染为 <table> HTML。
func (p *XLSParser) ParseWithResult(filename string, data []byte) ParseResult {
	f, err := excelize.OpenReader(bytes.NewReader(data))
	if err != nil {
		return ParseResult{Err: fmt.Errorf("xls open: %w", err)}
	}
	defer f.Close()

	var html strings.Builder
	html.WriteString("<html><body>")
	for _, sheet := range f.GetSheetList() {
		html.WriteString("<h3>")
		html.WriteString(sheet)
		html.WriteString("</h3>")
		rows, _ := f.GetRows(sheet)
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
		File:         map[string]any{"name": filename, "format": "xls"},
		HTML:         html.String(),
	}
}
// xls_parser.go — 旧版 .xls 解析：经 excelize 将各工作表渲染为 HTML 表格。
