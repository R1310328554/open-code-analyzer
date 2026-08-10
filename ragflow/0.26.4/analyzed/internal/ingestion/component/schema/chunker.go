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

package schema

// schema/chunker.go — 四种 Chunker 变体共享的上游/参数/输出 wire 类型。


import "fmt"

// ChunkerFromUpstream 为四种 chunker 变体共享的上游载荷。
// chunker variants (TokenChunker, TitleChunker, GroupTitleChunker,
// HierarchyTitleChunker).
//
// It mirrors the wire shape defined across two equivalent Pydantic
// schemas in the Python codebase:
//
//	rag/flow/chunker/schema.py:TokenChunkerFromUpstream
//	rag/flow/chunker/title_chunker/schema.py:TitleChunkerFromUpstream
//
// Both classes are field-identical (apart from the class name) — the
// variants share one upstream payload; their *Param structs differ.
//
// Wire shape (Pydantic):
//
//	created_time: float | None  (alias _created_time)
//	elapsed_time: float | None  (alias _elapsed_time)
//	name: str                   (required)
//	file: dict | None
//	chunks: list[dict] | None
//	output_format: Literal["json","markdown","text","html","chunks"] | None
//	json_result:      list[dict] | None  (alias "json")
//	markdown_result:  str        | None  (alias "markdown")
//	text_result:      str        | None  (alias "text")
//	html_result:      str        | None  (alias "html")
type ChunkerFromUpstream struct {
	CreatedTime *float64 `json:"_created_time,omitempty"`
	ElapsedTime *float64 `json:"_elapsed_time,omitempty"`

	// Name is the source document name. Required.
	Name string `json:"name"`

	// File is the optional upstream file descriptor.
	File *ChunkerFileMeta `json:"file,omitempty"`

	// Chunks is the upstream chunk list, set when output_format == "chunks".
	Chunks []ChunkDoc `json:"chunks,omitempty"`

	// OutputFormat controls which of the *Result fields below is the
	// active payload. Allowed values:
	//   "json"     -> JSONResult
	//   "markdown" -> MarkdownResult
	//   "text"     -> TextResult
	//   "html"     -> HTMLResult
	//   "chunks"   -> Chunks
	OutputFormat PayloadFormat `json:"output_format,omitempty"`

	// JSONResult is the upstream structured JSON list (alias "json" in
	// Python). Set when OutputFormat == "json".
	JSONResult []ChunkDoc `json:"json,omitempty"`

	// MarkdownResult is the upstream markdown payload (alias "markdown").
	// Set when OutputFormat == "markdown".
	MarkdownResult *string `json:"markdown,omitempty"`

	// TextResult is the upstream plain-text payload (alias "text").
	// Set when OutputFormat == "text".
	TextResult *string `json:"text,omitempty"`

	// HTMLResult is the upstream HTML payload (alias "html").
	// Set when OutputFormat == "html".
	HTMLResult *string `json:"html,omitempty"`
}

// Validate 强制 ChunkerFromUpstream 必填 name；output_format 与对应 *Result 一致。
// OutputFormat is not strictly required (defaults to "" and the
// component decides what to do with an empty payload), but the
// combination of `OutputFormat == "chunks"` with a non-nil Chunks is the
// happy path.
func (c *ChunkerFromUpstream) Validate() error {
	if c.Name == "" {
		return errRequiredField{Field: "name"}
	}
	if !c.OutputFormat.isKnown() {
		return errInvalidValue{Field: "output_format", Value: string(c.OutputFormat)}
	}
	switch c.OutputFormat {
	case PayloadFormatChunks:
		return nil
	case PayloadFormatJSON:
		if c.JSONResult == nil {
			return errRequiredField{Field: "json"}
		}
	case PayloadFormatMarkdown:
		if c.MarkdownResult == nil {
			return errRequiredField{Field: "markdown"}
		}
	case PayloadFormatText:
		if c.TextResult == nil {
			return errRequiredField{Field: "text"}
		}
	case PayloadFormatHTML:
		if c.HTMLResult == nil {
			return errRequiredField{Field: "html"}
		}
	}
	return nil
}

// ChunkerOutputs 为任意 chunker 变体的输出（output_format=chunks + chunks 列表）。
// four chunker components emit the same shape: a list of chunk maps
// under the "chunks" key, plus a marker output_format = "chunks".
//
// Mirrors what each chunker sets at the end of _invoke:
//
//	self.set_output("output_format", "chunks")
//	self.set_output("chunks", chunks)
type ChunkerOutputs struct {
	// OutputFormat is always "chunks" on success.
	OutputFormat PayloadFormat `json:"output_format,omitempty"`

	// Chunks is the produced chunk list. Each entry is a free-form map
	// mirroring the dict shape the Python code builds (text, doc_type_kwd,
	// tk_nums, PDF_POSITIONS_KEY, mom, img_id, etc.).
	Chunks []ChunkDoc `json:"chunks,omitempty"`

	// Error is set when the component short-circuits with an error
	// message (Python: set_output("_ERROR", ...)).
	Error string `json:"_ERROR,omitempty"`
}

// ---------------------------------------------------------------------------
// TokenChunkerParam — 镜像 Python TokenChunkerParam，含 delimiter_mode 等。
// ---------------------------------------------------------------------------
//
// Mirrors rag/flow/chunker/token_chunker.py:TokenChunkerParam.__init__.
// All fields are user-tunable; defaults match the Python values.

type TokenChunkerParam struct {
	// DelimiterMode 选择分块策略：token_size / delimiter / one。
	// Allowed values: "token_size", "delimiter", "one".
	DelimiterMode string `json:"delimiter_mode"`

	// ChunkTokenSize is the target chunk size in tokens.
	ChunkTokenSize int `json:"chunk_token_size"`

	// Delimiters is the list of split tokens. Strings wrapped in
	// backticks (e.g., "`\\n`") denote user-defined regex split points.
	Delimiters []string `json:"delimiters"`

	// OverlappedPercent is the chunk-overlap ratio in [0, 100).
	OverlappedPercent float64 `json:"overlapped_percent"`

	// ChildrenDelimiters is the secondary split applied to text chunks.
	ChildrenDelimiters []string `json:"children_delimiters"`

	// TableContextSize is the number of surrounding tokens to attach
	// to table chunks. 0 disables.
	TableContextSize int `json:"table_context_size"`

	// ImageContextSize is the number of surrounding tokens to attach
	// to image chunks. 0 disables.
	ImageContextSize int `json:"image_context_size"`
}

// Defaults 返回 Python 默认 TokenChunkerParam 值。
func (TokenChunkerParam) Defaults() TokenChunkerParam {
	return TokenChunkerParam{
		DelimiterMode:      "token_size",
		ChunkTokenSize:     512,
		Delimiters:         []string{"\n"},
		OverlappedPercent:  0,
		ChildrenDelimiters: []string{},
		TableContextSize:   0,
		ImageContextSize:   0,
	}
}

// Validate 强制与运行时组件一致的枚举/范围校验。
// at construction time, keeping the schema and component decoder aligned.
func (p TokenChunkerParam) Validate() error {
	switch p.DelimiterMode {
	case "token_size", "delimiter", "one":
	default:
		return errInvalidValue{Field: "delimiter_mode", Value: p.DelimiterMode}
	}
	if p.ChunkTokenSize <= 0 {
		return errInvalidValue{Field: "chunk_token_size", Value: fmt.Sprintf("%d", p.ChunkTokenSize)}
	}
	if p.OverlappedPercent < 0 || p.OverlappedPercent >= 1 {
		return errInvalidValue{Field: "overlapped_percent", Value: fmt.Sprintf("%v", p.OverlappedPercent)}
	}
	if p.TableContextSize < 0 {
		return errInvalidValue{Field: "table_context_size", Value: fmt.Sprintf("%d", p.TableContextSize)}
	}
	if p.ImageContextSize < 0 {
		return errInvalidValue{Field: "image_context_size", Value: fmt.Sprintf("%d", p.ImageContextSize)}
	}
	return nil
}

// ---------------------------------------------------------------------------
// TitleChunkerParam — 镜像 Python TitleChunkerParam，含 method/levels/hierarchy。
// ---------------------------------------------------------------------------
//
// Mirrors rag/flow/chunker/title_chunker/common.py:TitleChunkerParam.
// The class also reads `self.method` (set externally — see Python
// title_chunker.py:31-37 routing on `self._param.method`). The Go port
// captures it explicitly here. The component's `check()` enforces enum
// values.

type TitleChunkerParam struct {
	// Method 路由标题分块策略：hierarchy 或 group。
	// Allowed values: "hierarchy", "group".
	Method string `json:"method,omitempty"`

	// Levels is a list of regex-list groups, one per hierarchy level.
	// Each group is a list of regex strings; the component picks the
	// best-matching group at runtime.
	Levels [][]string `json:"levels"`

	// Hierarchy is the heading depth used by HierarchyTitleChunker.
	// Stored as a pointer to distinguish nil (unset) from 0.
	Hierarchy *int `json:"hierarchy,omitempty"`

	// IncludeHeadingContent, when true, makes the heading text part
	// of each emitted chunk.
	IncludeHeadingContent bool `json:"include_heading_content"`

	// RootChunkAsHeading, when true, prepends the root chunk's text
	// to every emitted chunk (and drops the root chunk itself).
	RootChunkAsHeading bool `json:"root_chunk_as_heading"`
}

// Defaults 返回 Python 默认 TitleChunkerParam（Method 由组件外部设置）。
// not initialized in the Python `__init__` (it is set externally); the
// default is left as the empty string and the component must supply it.
func (TitleChunkerParam) Defaults() TitleChunkerParam {
	return TitleChunkerParam{
		Levels:                [][]string{},
		Hierarchy:             nil,
		IncludeHeadingContent: false,
		RootChunkAsHeading:    false,
	}
}

// Validate 强制 Python check() 可表达的不变量（hierarchy 需 levels+hierarchy）。
// expressible in pure-data terms: when Method == "hierarchy" the
// hierarchy depth and level config must be present.
func (p *TitleChunkerParam) Validate() error {
	switch p.Method {
	case "hierarchy", "group":
	case "":
		return nil
	default:
		return errInvalidValue{Field: "method", Value: p.Method}
	}
	switch p.Method {
	case "hierarchy", "group":
		if len(p.Levels) == 0 {
			return errRequiredField{Field: "levels"}
		}
	}
	if p.Method == "hierarchy" && (p.Hierarchy == nil || *p.Hierarchy <= 0) {
		return errRequiredField{Field: "hierarchy"}
	}
	return nil
}

// ---------------------------------------------------------------------------
// GroupTitleChunkerParam / HierarchyTitleChunkerParam — Python 共用 TitleChunkerParam 的类型别名。
// ---------------------------------------------------------------------------
//
// In the Python codebase, both variants share the SAME
// `TitleChunkerParam` class — there is no per-variant param. The
// dispatch happens in title_chunker.py:31-37 by reading
// `self._param.method`.
//
// In Go we model the shared class as `TitleChunkerParam` and expose
// type aliases so component files can name the param type they
// actually use. This keeps the wire schema faithful to Python while
// giving each variant a self-documenting entry point in the registry.

type GroupTitleChunkerParam = TitleChunkerParam
type HierarchyTitleChunkerParam = TitleChunkerParam

// 四种 chunker 共享 ChunkerFromUpstream/ChunkerOutputs；变体差异在 Param 结构与组件实现文件。
