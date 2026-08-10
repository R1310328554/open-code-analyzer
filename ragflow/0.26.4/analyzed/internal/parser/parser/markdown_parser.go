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

// markdown_parser.go — Markdown 解析：gomarkdown AST 块级遍历。


import (
	"bytes"
	"fmt"
	"strings"

	"github.com/gomarkdown/markdown/ast"
	"github.com/gomarkdown/markdown/parser"
)

const (
	GoMarkdown = "go_markdown"
)

type MarkdownParser struct {
	libType string
}

func NewMarkdownParser(libType string) (*MarkdownParser, error) {
	switch libType {
	case GoMarkdown:
		return &MarkdownParser{
			libType: GoMarkdown,
		}, nil
	default:
		return nil, fmt.Errorf("unsupported Markdown library type: %s", libType)
	}
}

// ParseWithResult 实现 ParseResultProducer（计划 §6.5），
// 产出与 Python output_format=json 对齐的结构化 item 列表。
// 每个顶层块一条 {text, doc_type_kwd, ck_type}。
// 已移除 debug-print 路径，调用方直接消费 ParseResult。
// debug-print path has been removed; callers consume ParseResult directly.
func (p *MarkdownParser) ParseWithResult(filename string, data []byte) ParseResult {
	if p.libType != GoMarkdown {
		return ParseResult{Err: fmt.Errorf("unsupported Markdown library type: %s", p.libType)}
	}
	doc := markdownNew().Parse(data)

	var items []map[string]any
	walkMarkdownBlocks(doc, &items)
	if items == nil {
		// No blocks emitted — surface a single empty item so the
		// downstream chunker sees a non-nil JSON slice (mirrors the
		// Python contract of always producing at least one item).
		items = []map[string]any{{"text": "", "doc_type_kwd": "text"}}
	}
	return ParseResult{
		OutputFormat: "json",
		File: map[string]any{
			"name": filename,
		},
		JSON: items,
	}
}

func (p *MarkdownParser) String() string {
	return "MarkdownParser"
}

// markdownNew 集中定义 gomarkdown 扩展集（CommonExtensions 等）。
// in one place (both Parse and ParseWithResult consume it).
func markdownNew() *parser.Parser {
	extensions := parser.CommonExtensions | parser.AutoHeadingIDs | parser.NoEmptyLineBeforeBlock
	return parser.NewWithExtensions(extensions)
}

// walkMarkdownBlocks 遍历顶层块：Heading/Paragraph/List/CodeBlock 等。
// 标题带 ck_type=heading 供下游 title chunker 识别。
// downstream title chunker can find them; paragraphs (Paragraph)
// 遍历为 best-effort；完整 TOC/大纲待 deepdoc/parser 移植。
// best-effort pass — full TOC / outline handling lands with the
// deepdoc/parser port — but it satisfies the per-block "emit
// text+doc_type_kwd" contract enough for a Phase-1a migration
// test to verify wire-shape parity.
func walkMarkdownBlocks(doc ast.Node, out *[]map[string]any) {
	for _, child := range doc.GetChildren() {
		switch n := child.(type) {
		case *ast.Heading:
			*out = append(*out, map[string]any{
				"text":         headingText(n),
				"doc_type_kwd": "text",
				"ck_type":      "heading",
			})
		case *ast.Paragraph:
			*out = append(*out, map[string]any{
				"text":         leafText(n),
				"doc_type_kwd": "text",
				"ck_type":      "text",
			})
		case *ast.List:
			*out = append(*out, map[string]any{
				"text":         leafText(n),
				"doc_type_kwd": "text",
				"ck_type":      "list",
			})
		case *ast.CodeBlock:
			*out = append(*out, map[string]any{
				"text":         leafText(n),
				"doc_type_kwd": "text",
				"ck_type":      "code",
			})
		default:
			// Block types we don't yet normalize (HTML, tables,
			// images, definitions) are best-effort: emit the leaf
			// text without a ck_type so downstream components can
			// still treat them as text chunks.
			txt := leafText(n)
			if strings.TrimSpace(txt) != "" {
				*out = append(*out, map[string]any{
					"text":         txt,
					"doc_type_kwd": "text",
				})
			}
		}
	}
}

// headingText 拼接 Heading 内联 Text 子节点为标题字符串。
// concatenating every Leaf / Text child. Empty headings emit "".
func headingText(h *ast.Heading) string {
	var buf bytes.Buffer
	for _, c := range h.GetChildren() {
		buf.WriteString(leafText(c))
	}
	return strings.TrimSpace(buf.String())
}

// leafText 递归收集 Text/Code 字面量，TrimSpace 后返回。
// leaf (Text or Inline content) and returns the concatenated UTF-8.
// Non-text containers that have no leaf descendants return "".
func leafText(n ast.Node) string {
	var buf bytes.Buffer
	walkLeaf(n, &buf)
	return strings.TrimSpace(buf.String())
}

func walkLeaf(n ast.Node, buf *bytes.Buffer) {
	switch t := n.(type) {
	case *ast.Text:
		buf.Write(t.Literal)
	case *ast.Code:
		buf.Write(t.Literal)
	default:
		for _, c := range n.GetChildren() {
			walkLeaf(c, buf)
		}
	}
}

// MarkdownParser lib_type=go_markdown；空文档仍产出单条空 item 满足 Python 契约。
