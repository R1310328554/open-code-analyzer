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
)

// PPTParser 旧版 .ppt 二进制解析器，底层委托 PPTX 路径。
type PPTParser struct {
	libType string
}

// NewPPTParser 目前仅支持 OfficeOxide 库类型。
func NewPPTParser(libType string) (*PPTParser, error) {
	switch libType {
	case OfficeOxide:
		return &PPTParser{
			libType: OfficeOxide,
		}, nil
	default:
		return nil, fmt.Errorf("unsupported PPT library type: %s", libType)
	}
}

func (p *PPTParser) String() string {
	return "PPTParser"
}

// ParseWithResult 委托 PPTXParser 输出结构化 JSON；Python slides 分支对 ppt/pptx 统一处理。
func (p *PPTParser) ParseWithResult(filename string, data []byte) ParseResult {
	delegate, err := NewPPTXParser(OfficeOxide)
	if err != nil {
		return ParseResult{Err: fmt.Errorf("ppt delegate: %w", err)}
	}
	res := delegate.ParseWithResult(filename, data)
	if res.File != nil {
		res.File["format"] = "ppt"
	}
	return res
}
// ppt_parser.go — 旧版 PPT 解析器，委托 PPTX 路径并标记 format=ppt。
