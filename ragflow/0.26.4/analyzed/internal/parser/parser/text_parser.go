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

// TextParser（Phase 2.5 Slice 1）— 纯文本与代码文件族解析器。
//
// The python rag/flow/parser/parser.py:_code path (L1066) routes
// .txt / .py / .js / .java / .c / .cpp / .h / .php / .go / .ts / .sh
// / .cs / .kt / .sql files through deepdoc.parser.TxtParser. The Go
// side needs a parser for these families so `text&code` resolves to a
// real ParseResultProducer.
//
// TextParser fills that gap with a minimal but real implementation:
// it splits the input into paragraph-sized items and emits the
// python-compatible `{text, doc_type_kwd:"text"}` shape. The
// python TxtParser additionally does layout-aware section
// detection; the Go version is intentionally simpler because (a)
// no production template currently relies on text&code for richer
// structure than paragraph items.

package parser

import (
	"bytes"
	"strings"
)

const TextParserLibType = "text"

// TextParser 处理 text&code 文件族，直接实现 ParseResultProducer 结构化输出契约。
type TextParser struct {
	// maxItemBytes 限制单条输出文本字节上限；8192 防止下游分块器收到过大段落。
	maxItemBytes int
}

// NewTextParser 构造 TextParser；libType 仅为与其他解析器构造函数签名一致，实际忽略。
func NewTextParser(_ string) (*TextParser, error) {
	return &TextParser{maxItemBytes: 8192}, nil
}

// ParseWithResult 按非空段落逐条输出 JSON 项（text + doc_type_kwd），对齐 Python TxtParser。
//
// The items slice is always non-nil so downstream chunkers see a
// non-empty JSON payload even for an empty input (mirrors the
// MarkdownParser convention at markdown_parser.go:71-76).
func (p *TextParser) ParseWithResult(filename string, data []byte) ParseResult {
	if !utf8Valid(data) {
		return ParseResult{Err: errInvalidUTF8}
	}
	items := textParserItems(data, p.maxItemBytes)
	if items == nil {
		items = []map[string]any{{"text": "", "doc_type_kwd": "text"}}
	}
	return ParseResult{
		OutputFormat: "json",
		File: map[string]any{
			"name":     filename,
			"size":     len(data),
			"encoding": "utf-8",
		},
		JSON: items,
	}
}

func (p *TextParser) String() string {
	return "TextParser"
}

// errInvalidUTF8 在输入非合法 UTF-8 时返回，与 Python 侧显式报错行为一致。
var errInvalidUTF8 = errInvalidUTF8Sentinel("parser: text input is not valid UTF-8")

type errInvalidUTF8Sentinel string

func (e errInvalidUTF8Sentinel) Error() string { return string(e) }

// utf8Valid 轻量 UTF-8 校验，规则与 unicode/utf8.Valid 相同。
func utf8Valid(data []byte) bool {
	for i := 0; i < len(data); {
		r, size := decodeRune(data[i:])
		if r == 0xFFFD && size == 1 {
			return false
		}
		i += size
	}
	return true
}

// decodeRune 最小 UTF-8 解码器，无效序列返回 (RuneError, 1)。
func decodeRune(p []byte) (rune, int) {
	if len(p) == 0 {
		return 0xFFFD, 0
	}
	c := p[0]
	switch {
	case c < 0x80:
		return rune(c), 1
	case c < 0xC2:
		return 0xFFFD, 1
	case c < 0xE0:
		if len(p) < 2 || p[1]&0xC0 != 0x80 {
			return 0xFFFD, 1
		}
		return rune(c&0x1F)<<6 | rune(p[1]&0x3F), 2
	case c < 0xF0:
		if len(p) < 3 || p[1]&0xC0 != 0x80 || p[2]&0xC0 != 0x80 {
			return 0xFFFD, 1
		}
		return rune(c&0x0F)<<12 | rune(p[1]&0x3F)<<6 | rune(p[2]&0x3F), 3
	case c < 0xF5:
		if len(p) < 4 || p[1]&0xC0 != 0x80 || p[2]&0xC0 != 0x80 || p[3]&0xC0 != 0x80 {
			return 0xFFFD, 1
		}
		return rune(c&0x07)<<18 | rune(p[1]&0x3F)<<12 | rune(p[2]&0x3F)<<6 | rune(p[3]&0x3F), 4
	}
	return 0xFFFD, 1
}

// textParserItems 按空行分段；超长段在 maxItemBytes 处再切分。
func textParserItems(data []byte, maxItemBytes int) []map[string]any {
	var items []map[string]any
	for _, raw := range bytes.Split(data, []byte("\n\n")) {
		text := strings.TrimSpace(string(raw))
		if text == "" {
			continue
		}
		if maxItemBytes > 0 && len(text) > maxItemBytes {
			// 优先在 maxItemBytes 内最近换行处切分，否则硬切。
			cut := strings.LastIndex(text[:maxItemBytes], "\n")
			if cut <= 0 {
				cut = maxItemBytes
			}
			items = append(items, map[string]any{
				"text":         strings.TrimSpace(text[:cut]),
				"doc_type_kwd": "text",
			})
			text = strings.TrimSpace(text[cut:])
			if text == "" {
				continue
			}
		}
		items = append(items, map[string]any{
			"text":         text,
			"doc_type_kwd": "text",
		})
	}
	return items
}
// text_parser.go — 纯文本与代码文件解析器：按段落分块输出 Python 兼容的 JSON 条目（text + doc_type_kwd）。
