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

package parser

import (
	"fmt"
	"strings"

	officeOxide "github.com/yfedoseev/office_oxide/go"
)

// PPTXParser 使用 office_oxide 提取幻灯片纯文本。
type PPTXParser struct {
	libType string
}

// NewPPTXParser 目前仅支持 OfficeOxide 库类型。
func NewPPTXParser(libType string) (*PPTXParser, error) {
	switch libType {
	case OfficeOxide:
		return &PPTXParser{
			libType: OfficeOxide,
		}, nil
	default:
		return nil, fmt.Errorf("unsupported PPTX library type: %s", libType)
	}
}

func (p *PPTXParser) String() string {
	return "PPTXParser"
}

// ParseWithResult 每张幻灯片输出一条 JSON section；Python slides 分支固定 output_format=json。
func (p *PPTXParser) ParseWithResult(filename string, data []byte) ParseResult {
	doc, err := officeOxide.OpenFromBytes(data, "pptx")
	if err != nil {
		return ParseResult{Err: fmt.Errorf("pptx open: %w", err)}
	}
	defer doc.Close()

	text, err := doc.PlainText()
	if err != nil {
		return ParseResult{Err: fmt.Errorf("pptx plain-text: %w", err)}
	}

	// 按 form-feed（\f）分块，对齐 Python TxtParser/slides 解析约定。
	var items []map[string]any
	for i, raw := range strings.Split(text, "\f") {
		trimmed := strings.TrimSpace(raw)
		if trimmed == "" {
			continue
		}
		items = append(items, map[string]any{
			"text":         trimmed,
			"doc_type_kwd": "text",
			"slide_number": i + 1,
		})
	}
	if items == nil {
		items = []map[string]any{{"text": strings.TrimSpace(text), "doc_type_kwd": "text"}}
	}

	return ParseResult{
		OutputFormat: "json",
		File:         map[string]any{"name": filename, "format": "pptx"},
		JSON:         items,
	}
}
// pptx_parser.go — PPTX 幻灯片逐页纯文本提取，按 form-feed 分块输出 JSON。
