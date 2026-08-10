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

	officeOxide "github.com/yfedoseev/office_oxide/go"
)

type DOCXParser struct {
	libType string
}

func NewDOCXParser(libType string) (*DOCXParser, error) {
	switch libType {
	case OfficeOxide:
		return &DOCXParser{
			libType: OfficeOxide,
		}, nil
	default:
		return nil, fmt.Errorf("unsupported DOCX library type: %s", libType)
	}
}

// ParseWithResult 调用 office_oxide ToMarkdown 产出 DOCX Markdown。
// 返回 OutputFormat=markdown，镜像 Python Docx 分支 wire 形状。
// the rendered Markdown on the matching key. Mirrors the python
// parser.py:Docx branch which uses rag.app.naive.Docx to render
// Go 侧暂委托 office_oxide，后续可换原生 Markdown 渲染器。
// will switch to a native Markdown renderer in a follow-up.
func (p *DOCXParser) ParseWithResult(filename string, data []byte) ParseResult {
	doc, err := officeOxide.OpenFromBytes(data, "docx")
	if err != nil {
		return ParseResult{Err: fmt.Errorf("docx open: %w", err)}
	}
	defer doc.Close()

	md, err := doc.ToMarkdown()
	if err != nil {
		return ParseResult{Err: fmt.Errorf("docx to-markdown: %w", err)}
	}

	return ParseResult{
		OutputFormat: "markdown",
		File:         map[string]any{"name": filename, "format": "docx"},
		Markdown:     md,
	}
}

func (p *DOCXParser) String() string {
	return "DOCXParser"
}

// DOCXParser 与 DOCParser 同属 Office 家族，共享 OfficeOxide lib_type 标识。
