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

// ParserFromUpstream 是 Parser 组件消费的上游载荷，镜像 Python ParserFromUpstream。
//
//	created_time: float | None  (alias _created_time)
//	elapsed_time: float | None  (alias _elapsed_time)
//	name: str                   (required)
//	file: dict | None
//	abstract: bool = False
//	author:  bool = False
type ParserFromUpstream struct {
	CreatedTime *float64 `json:"_created_time,omitempty"`
	ElapsedTime *float64 `json:"_elapsed_time,omitempty"`

	Name string `json:"name"`

	// File is the optional upstream file descriptor. Python allows None
	// when the parser is invoked via a canvas-bound doc_id path.
	File map[string]any `json:"file,omitempty"`

	Abstract bool `json:"abstract,omitempty"`
	Author   bool `json:"author,omitempty"`
}

// Validate 强制 Name 必填；非空时返回 nil。
func (p *ParserFromUpstream) Validate() error {
	if p.Name == "" {
		return errRequiredField{Field: "name"}
	}
	return nil
}

// Page 为单页解析结果；Python 使用 dict 字面量，Go 以 map 保持 wire 类型而不过度约束形状。
type Page map[string]any

// ParserSetup 为 ParserParam.setups[fileType] 的 per-filetype 配置块，键 heterogeneous 故用自由 map。
type ParserSetup map[string]any

// ParserParam 是 Parser 组件静态配置，镜像 Python ParserParam。
//
// Two top-level fields are configured in the Python class:
//
//	setups: dict[str, dict]  (one entry per file type: pdf, docx, ...)
//	allowed_output_format: dict[str, list[str]]  (per-file-type formats)
//
// `check()` runs further validation that is intentionally NOT
// replicated here — Validate() enforces wire-shape only; business-rule
// validation lives in the component implementation (Phase 2.2).
type ParserParam struct {
	// Setups holds the per-file-type parser config. Keys are file-type
	// identifiers ("pdf", "docx", "markdown", "spreadsheet", "image",
	// "audio", "video", "email", "epub", "doc", "text&code", "html",
	// "slides"); values are free-form config blobs.
	Setups map[string]ParserSetup `json:"setups"`

	// AllowedOutputFormat mirrors `allowed_output_format` from the
	// Python class. Used for client-side input-form validation.
	AllowedOutputFormat map[string][]string `json:"allowed_output_format"`
}

// Defaults 返回 Python 默认 ParserParam（完整 setups 表与 allowed_output_format）。
func (ParserParam) Defaults() ParserParam {
	return ParserParam{
		AllowedOutputFormat: map[string][]string{
			"pdf":         {"json", "markdown"},
			"spreadsheet": {"json", "markdown", "html"},
			"doc":         {"json", "markdown"},
			"docx":        {"json", "markdown"},
			"slides":      {"json"},
			"image":       {"json"},
			"email":       {"text", "json"},
			"markdown":    {"text", "json"},
			"text&code":   {"text", "json"},
			"html":        {"text", "json"},
			"audio":       {"json"},
			"video":       {},
			"epub":        {"text", "json"},
		},
		Setups: map[string]ParserSetup{
			"pdf": {
				"parse_method":          "deepdoc",
				"lang":                  "Chinese",
				"flatten_media_to_text": false,
				"remove_toc":            false,
				"remove_header_footer":  false,
				"suffix":                []string{"pdf"},
				"output_format":         "json",
			},
			"spreadsheet": {
				"parse_method":          "deepdoc",
				"flatten_media_to_text": false,
				"output_format":         "html",
				"suffix":                []string{"xls", "xlsx", "csv"},
			},
			"doc": {
				"remove_toc":           false,
				"remove_header_footer": false,
				"suffix":               []string{"doc"},
				"output_format":        "json",
			},
			"docx": {
				"flatten_media_to_text": false,
				"remove_toc":            false,
				"remove_header_footer":  false,
				"suffix":                []string{"docx"},
				"output_format":         "json",
			},
			"markdown": {
				"flatten_media_to_text": false,
				"suffix":                []string{"md", "markdown", "mdx"},
				"remove_toc":            false,
				"output_format":         "json",
			},
			"text&code": {
				"suffix": []string{
					"txt", "py", "js", "java", "c", "cpp", "h", "php",
					"go", "ts", "sh", "cs", "kt", "sql",
				},
				"output_format": "json",
			},
			"html": {
				"suffix":               []string{"htm", "html"},
				"remove_toc":           false,
				"remove_header_footer": false,
				"output_format":        "json",
			},
			"slides": {
				"parse_method":  "deepdoc",
				"suffix":        []string{"pptx", "ppt"},
				"output_format": "json",
			},
			"image": {
				"parse_method":  "ocr",
				"llm_id":        "",
				"lang":          "Chinese",
				"system_prompt": "",
				"suffix":        []string{"jpg", "jpeg", "png", "gif"},
				"output_format": "json",
			},
			"email": {
				"suffix": []string{"eml", "msg"},
				"fields": []string{
					"from", "to", "cc", "bcc", "date", "subject",
					"body", "attachments", "metadata",
				},
				"output_format": "text",
			},
			"audio": {
				"suffix": []string{
					"da", "wave", "wav", "mp3", "aac", "flac", "ogg",
					"aiff", "au", "midi", "wma", "realaudio", "vqf",
					"oggvorbis", "ape",
				},
				"output_format": "text",
			},
			"video": {
				"suffix":        []string{"mp4", "avi", "mkv"},
				"output_format": "text",
				"prompt":        "",
			},
			"epub": {
				"suffix":        []string{"epub"},
				"output_format": "json",
			},
		},
	}
}

// Validate 返回 nil；业务校验（如 parse_method 枚举）在组件实现中执行。
func (ParserParam) Validate() error { return nil }

// ParserOutputs 是 Parser 组件调用结果，为下游消费的 typed wire 表面。
//
// Mirrors what Parser sets at rag/flow/parser/parser.py:_invoke. The
// parser writes to EITHER ("json" | "markdown" | "text" | "html") and
// always sets "output_format" + "file" + "_ERROR".
type ParserOutputs struct {
	// OutputFormat is the active output format for this run
	// (one of "json", "markdown", "text", "html"). The downstream
	// Tokenizer branches on this field.
	OutputFormat string `json:"output_format,omitempty"`

	// JSON 在 output_format == "json" 时存放结构化 section 列表。
	JSON []map[string]any `json:"json,omitempty"`

	// Markdown 在 output_format == "markdown" 时存放渲染结果。
	Markdown string `json:"markdown,omitempty"`

	// Text 在 output_format == "text" 时存放纯文本。
	Text string `json:"text,omitempty"`

	// HTML 在 output_format == "html" 时存放 HTML。
	HTML string `json:"html,omitempty"`

	// File is the upstream file descriptor with parser-derived metadata
	// (e.g., outlines) merged in. Mirrors the Python `set_output("file", ...)`
	// at parser.py:609, 791, 828.
	File map[string]any `json:"file,omitempty"`

	// Error is set when the component short-circuits with an error
	// message (Python: set_output("_ERROR", ...)).
	Error string `json:"_ERROR,omitempty"`
}
// schema/parser.go — Parser 组件上下游 wire 类型与默认配置表。
