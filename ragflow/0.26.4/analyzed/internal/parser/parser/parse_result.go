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

// ParseResult 为 Go parser 库的结构化输出契约（port-rag-flow-pipeline §6.5）。
// 要求 parsers 产出足够数据以重建 Python 兼容的阶段边界载荷。
// parsers surface enough data to reconstruct a Python-compatible
// stage-boundary payload:
//
//	output_format ∈ {"json","markdown","text","html"}
//	file         (enriched metadata)
//	exactly one payload family populated (matching output_format)
//	err
//
// Go 调用方仅消费 ParseResult；旧 Parse(filename,[]byte) error 接口已移除。
// contract. The legacy `Parse(filename, []byte) error` interface has
// been removed so parser dispatch, ingestion, and service paths all
// share the same typed payload contract.

package parser

// ParseResult 成功时恰好填充一种 payload 族（JSON/Markdown/Text/HTML）。
// Exactly one of the payload fields (JSON / Markdown / Text / HTML)
// 失败时 Err 非 nil，各 payload 字段均为零值。
// port-rag-flow-pipeline-to-go.md §4.2:
//
//   - OutputFormat = "json"     → JSON populated
//   - OutputFormat = "markdown" → Markdown populated
//   - OutputFormat = "text"     → Text populated
//   - OutputFormat = "html"     → HTML populated
//
// On failure (Err != nil), all payload fields are zero values and
// OutputFormat is empty.
type ParseResult struct {
	// OutputFormat 为 parser 选择的 wire 格式；Err 非 nil 时为空。
	// chose. Empty when Err is non-nil.
	OutputFormat string

	// File 为 parser 富化的文件元数据（如 outline、page_count 等）。
	// Python this is the dict form of the original `file`
	// descriptor, augmented with format-specific keys (e.g.
	// `outline` on the PDF path, `page_count` for paginated
	// formats). Nil when the parser did not enrich.
	File map[string]any

	// JSON 在 OutputFormat=json 时填充；形状因 parser 家族而异。
	// PDF 产出带 text/doc_type_kwd 的 []map；markdown/html 产出规范化 item。
	// `[]map[string]any` with `text` + `doc_type_kwd` keys (and
	// optional `image` / `layout` / `positions` fields);
	// markdown / html / text emit normalized
	// `{text, doc_type_kwd}` items; image emits OCR/VLM result
	// items. Exactly one payload family is populated on success.
	JSON []map[string]any

	// Markdown 在 OutputFormat=markdown 时填充字符串载荷。
	// "markdown". Empty otherwise.
	Markdown string

	// Text 在 OutputFormat=text 时填充纯文本载荷。
	// Empty otherwise.
	Text string

	// HTML 在 OutputFormat=html 时填充 HTML 字符串载荷。
	// Empty otherwise.
	HTML string

	// Err 为失败原因；非 nil 时所有 payload 字段为零值。
	// fields are zero values.
	Err error
}

// ParseResultProducer 为 parser 包唯一结构化输出接口；GetParser 返回值须实现之。
// contract. Every parser returned by GetParser must implement it.
type ParseResultProducer interface {
	ParseWithResult(filename string, data []byte) ParseResult
}

// ParseResult 统一 dispatch、ingestion 与 service 路径的类型化载荷契约。
