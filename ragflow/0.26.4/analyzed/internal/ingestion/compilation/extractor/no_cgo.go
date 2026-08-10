//go:build !cgo_thincner

//
//  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
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

// 非 CGO 构建的桩实现。
//
// C++ ThincNER/ThincParser/ThincTagger 经 cgo 接入（ner.go、parser_go.go 等，build tag cgo_thincner）。
// 无 cgo_thincner 时测试二进制无法链接 ThincNER_* 等符号。
//
// 本文件声明包其余部分依赖的导出面（Entity/Relation/Extractor 等），
// 使 dep_relation.go 等纯 Go 文件继续编译。
//
// cgo 函数返回 ErrNoCGO，无 CGO 构建时调用方显式失败而非静默降级。

package extractor

import (
	"encoding/json"
	"errors"
	"sync"
)

// ErrNoCGO 非 CGO 构建时所有 cgo 入口返回此错误；
// 需 C++ 静态库 librag_tokenizer_c_api.a 才能推理。
var ErrNoCGO = errors.New("extractor: CGO disabled — ThincNER / ThincParser / ThincTagger unavailable")

// Entity 镜像 cgo 版声明。
type Entity struct {
	Text       string         `json:"text"`
	Label      string         `json:"label"`
	StartChar  int            `json:"start_char"`
	EndChar    int            `json:"end_char"`
	Confidence float64        `json:"confidence"`
	AppType    string         `json:"app_type,omitempty"`
	Metadata   map[string]any `json:"metadata,omitempty"`
}

// Relation 镜像 cgo 版声明。
type Relation struct {
	Subject    Entity         `json:"subject"`
	Predicate  string         `json:"predicate"`
	Object     Entity         `json:"object"`
	Confidence float64        `json:"confidence"`
	Context    string         `json:"context,omitempty"`
	Metadata   map[string]any `json:"metadata,omitempty"`
}

// ExtractionResult 镜像 cgo 版声明。
type ExtractionResult struct {
	Entities  []Entity       `json:"entities"`
	Relations []Relation     `json:"relations"`
	Language  string         `json:"language,omitempty"`
	Metadata  map[string]any `json:"metadata,omitempty"`
}

// Extractor 镜像 cgo 版；推理调用返回 ErrNoCGO，其余接口一致。
type Extractor struct {
	mu                  sync.Mutex
	Lang                string
	ConfidenceThreshold float64
	IncludeTokens       bool
}

// ModelPredictor 测试注入 seam，no-CGO 镜像类型以保持测试编译。
type ModelPredictor func(tokensJSON string) (string, error)

// NewExtractor 返回 no-CGO Extractor；DepExtractRelations/ExtractRelations 不依赖 cgo。
func NewExtractor(lang string) *Extractor {
	return &Extractor{Lang: lang}
}

// RunParser no-CGO 桩，cgo 实现在 parser_go.go。
func RunParser(nerDir, parserDir string, tokensJSON string) (string, error) {
	return "", ErrNoCGO
}

// RunTagger no-CGO 桩，cgo 实现在 parser_go.go。
func RunTagger(nerDir, taggerDir string, tokensJSON string) (string, error) {
	return "", ErrNoCGO
}

// ParseTokensWithParser RunParser 的类型化包装桩；调用方应先检查 error。
func ParseTokensWithParser(nerDir, parserDir string, tokens []string) ([]DepTokenC, error) {
	tj, _ := json.Marshal(tokens)
	resultJSON, err := RunParser(nerDir, parserDir, string(tj))
	if err != nil {
		return nil, err
	}
	var tokensC []DepTokenC
	if err := json.Unmarshal([]byte(resultJSON), &tokensC); err != nil {
		return nil, err
	}
	return tokensC, nil
}

// DepTokenC 镜像 cgo 版声明。
type DepTokenC struct {
	Text  string `json:"text"`
	Head  int    `json:"head"`
	Dep   string `json:"dep"`
	Index int    `json:"index"`
}

// DetectLanguage 纯 Go 实现，与 CGO 版一致；此处声明使 no-CGO 路径自包含。
func DetectLanguage(text string) string {
	return detectLanguageNoCGO(text)
}

// detectLanguageNoCGO 纯 Go 语言检测，无 cgo 依赖。
//
// 检测器优先最具体的 Unicode 范围：
//
//   - Hiragana / Katakana → "ja" (Japanese)
//   - CJK Unified Ideographs → "zh" (Chinese)
//   - Hangul Syllables → "ko" (Korean)
//   - Cyrillic → "ru"
//   - Arabic → "ar"
//   - Devanagari → "hi"
//   - Otherwise → "en"
//
// 与生产启发式足够接近供 no-CGO 测试链接；高精度场景请用 -tags=cgo_thincner。
func detectLanguageNoCGO(text string) string {
	if len(text) == 0 {
		return "en"
	}
	var ja, zh, ko, cyrillic, arabic, devanagari, latin int
	for _, r := range text {
		switch {
		case r >= 0x3040 && r <= 0x309F: // Hiragana
			ja++
		case r >= 0x30A0 && r <= 0x30FF: // Katakana
			ja++
		case r >= 0x4E00 && r <= 0x9FFF: // CJK Unified
			zh++
		case r >= 0xAC00 && r <= 0xD7AF: // Hangul
			ko++
		case r >= 0x0400 && r <= 0x04FF: // Cyrillic
			cyrillic++
		case r >= 0x0600 && r <= 0x06FF: // Arabic
			arabic++
		case r >= 0x0900 && r <= 0x097F: // Devanagari
			devanagari++
		case (r >= 'A' && r <= 'Z') || (r >= 'a' && r <= 'z'):
			latin++
		}
	}
	switch {
	case ja > 0:
		return "ja"
	case zh > 0:
		return "zh"
	case ko > 0:
		return "ko"
	case cyrillic > 0:
		return "ru"
	case arabic > 0:
		return "ar"
	case devanagari > 0:
		return "hi"
	default:
		return "en"
	}
}
