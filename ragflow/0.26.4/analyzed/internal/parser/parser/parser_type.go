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

// parser_type.go — 解析器工厂：按 utility.FileType 与 lib_type 分发具体 parser。


import (
	"fmt"
	"ragflow/internal/utility"
)

// GetParser 根据文件类型与 config.lib_type 构造 ParseResultProducer 实例。
func GetParser(fileType utility.FileType, config map[string]string) (ParseResultProducer, error) {
	libType, ok := config["lib_type"]
	if !ok {
		return nil, fmt.Errorf("missing lib_type config")
	}
	switch fileType {
	case utility.FileTypePPTX:
		return NewPPTXParser(libType)
	case utility.FileTypePPT:
		return NewPPTParser(libType)
	case utility.FileTypeXLSX:
		return NewXLSXParser(libType)
	case utility.FileTypeXLS:
		return NewXLSParser(libType)
	case utility.FileTypeDOCX:
		return NewDOCXParser(libType)
	case utility.FileTypeDOC:
		return NewDOCParser(libType)
	case utility.FileTypePDF:
		return NewPDFParser(), nil
	case utility.FileTypeHTML:
		return NewHTMLParser(Official)
	case utility.FileTypeMarkdown:
		return NewMarkdownParser(GoMarkdown)
	case utility.FileTypeTXT:
		return NewTextParser(libType)
	default:
		return nil, fmt.Errorf("unsupported file type: %s", fileType)
	}
}

// GetParser 覆盖 Office/PDF/HTML/Markdown/TXT 等家族；HTML 固定 Official、Markdown 固定 GoMarkdown。
