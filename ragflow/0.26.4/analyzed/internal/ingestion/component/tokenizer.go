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

// Tokenizer ingestion 组件（Phase 2.4）。移植 Python rag/flow/tokenizer/tokenizer.py：
// (a) 经 Go tokenizer 包计算全文 token 数；(b) 经租户嵌入模型计算向量。
//
// 职责范围（如实说明）：
//
//   - TOKEN 计数：wire 级对齐。search_method 含 full_text 时为 chunk 写入
//     content_ltks/content_sm_ltks 及 title_tks/title_sm_tks；Go 经 internal/tokenizer
//     调用同一 C++ 绑定；CJK 输入时 CGo 分析器未初始化则返回空串。
//
//   - CJK 注意：NumTokensFromString 在 tiktoken 初始化失败时回退 len([]byte(s))（CJK 可能高估）；
//     Go 移植保留该行为，tokenizer 包为计数唯一真相源。 Test
//     `TestTokenizerComponent_Invoke_Unicode` asserts only that the
//     count is finite and non-negative, matching the test
//     convention in plan §6 (coverage target:
//     "Tokenizer returns finite token counts for empty / unicode /
//     mixed-script text").
//
//   - 嵌入模型解析：镜像 Python LLMBundle.encode；Go 经 EncodeFunc 注入，避免 ingestion 反向依赖 service。
//     The component does NOT directly construct a model driver —
//     the resolution path depends on tenant/DAO context that lives
//     in `internal/service`, and importing `internal/service` from
//     `internal/ingestion/component` would invert the dependency
//     direction (plan §3 import graph: ingestion → agent/runtime
//     only). The injection point is `EncodeFunc` (package-level
//     var); production wires it in `main()` (or an analogous
//     bootstrap step) and tests inject a stub. When `EncodeFunc` is
//     nil the component short-circuits the embedding branch with
//     a clear error — the same fail-loud contract the Python side
//     enforces via `LLMBundle` constructor.
//
//   - 批量嵌入（AD-5a）：Go 一次 Encode([]string) 覆盖全部 chunk，Parallelism=1。 Drivers that need to chunk internally
//     can do so — the wire call is one round-trip.
//
//   - 追踪：WithTimeout(60s)、TrackProgress、TrackElapsed。 See
//     `internal/agent/runtime/helpers.go` (plan §1 Phase 1).
//
//   - 未移植项：
//
//   - Python finalize_pdf_chunk 后处理归 Parser 组件（Phase 2.2）。
//
//   - Python thread_pool_exec + embed_limiter — 由单次批量 Encode 替代。
package component

import (
	"context"
	"encoding/json"
	"fmt"
	"regexp"
	"strings"
	"time"

	"ragflow/internal/agent/runtime"
	"ragflow/internal/ingestion/component/schema"
	"ragflow/internal/tokenizer"
)

const ComponentNameTokenizer = "Tokenizer"

// tokenizerTimeout 限制批量嵌入调用耗时，镜像 Python @timeout(60)；测试可缩短。
var tokenizerTimeout = 60 * time.Second

// titleExtRE 分词前去除文档名尾部扩展名，镜像 Python re.sub。
var titleExtRE = regexp.MustCompile(`\.[a-zA-Z]+$`)

// htmlTableRE 去除 HTML 表格标签，避免嵌入文本携带原始 markup。
var htmlTableRE = regexp.MustCompile(`</?(table|td|caption|tr|th)( [^<>]{0,12})?>`)

// Embedder 为嵌入分支的可测试 seam；生产经 ModelProviderService 解析模型，测试注入 stub。
//
// Returning one vector per input text (length len(texts), each
// vector non-empty) is the contract; nil/error halts the component.
type Embedder interface {
	Encode(texts []string) ([][]float64, error)
}

// EncodeFunc 为包级注入点；nil 表示禁用嵌入；main 设置一次，测试可替换 stub。
var EncodeFunc func(tenantID, embdID string) Embedder

// TokenizerComponent 为上游 chunk 列表计算 token 数与（可选）嵌入向量。
//
// Inputs:
//
//	tenant_id  (string, optional) — used to resolve the embedding model
//	model_id   (string, optional) — explicit override; falls back to
//	                             Param.EmbeddingID (future)
//	output_format (string) — one of json/markdown/text/html/chunks
//	chunks        (list[map]) — chunk list when output_format == "chunks"
//	json          (list[map]) — structured parser payload when output_format == "json" or unset
//	markdown/text/html        — scalar payload matching output_format
//
// Outputs:
//
//	chunks                       — the chunk list with tokenized fields
//	                              and (when embedding is requested)
//	                              q_<n>_vec vector fields
//	embedding_token_consumption  — non-negative int (matches the python
//	                              `embedding_token_consumption` output)
//	output_format                — always "chunks" (matches python set_output)
//	_created_time / _elapsed_time — TrackElapsed bookkeeping
type TokenizerComponent struct {
	param schema.TokenizerParam
}

// NewTokenizerComponent 从 DSL 参数构造组件，镜像 Python TokenizerParam 默认值。
func NewTokenizerComponent(params map[string]any) (runtime.Component, error) {
	p := schema.TokenizerParam{}.Defaults()
	if params != nil {
		if v, ok := params["search_method"]; ok {
			// Replace (not append) so a caller-supplied
			// search_method = ["full_text"] correctly disables
			// embedding. Python's TokenizerParam similarly treats
			// caller-supplied values as the full set.
			p.SearchMethod = nil
			switch t := v.(type) {
			case []any:
				for _, x := range t {
					if s, ok := x.(string); ok {
						p.SearchMethod = append(p.SearchMethod, s)
					}
				}
			case []string:
				p.SearchMethod = append(p.SearchMethod, t...)
			}
		}
		if v, ok := params["filename_embd_weight"]; ok {
			switch t := v.(type) {
			case float64:
				p.FilenameEmbdWeight = t
			case int:
				p.FilenameEmbdWeight = float64(t)
			}
		}
		if v, ok := params["fields"]; ok {
			switch t := v.(type) {
			case string:
				p.Fields = []string{t}
			case []any:
				for _, x := range t {
					if s, ok := x.(string); ok {
						p.Fields = append(p.Fields, s)
					}
				}
			case []string:
				p.Fields = append(p.Fields, t...)
			}
		}
	}
	if err := p.Validate(); err != nil {
		return nil, fmt.Errorf("Tokenizer: param check: %w", err)
	}
	return &TokenizerComponent{param: p}, nil
}

// Inputs 返回输入参数元数据。
func (c *TokenizerComponent) Inputs() map[string]string {
	return map[string]string{
		"tenant_id":     "Tenant identifier used to resolve the embedding model (mirrors python self._canvas._tenant_id).",
		"model_id":      "Optional explicit embedding-model override. Falls back to EncodeFunc resolution when unset.",
		"output_format": "Upstream payload discriminator: json / markdown / text / html / chunks.",
		"chunks":        "List of chunk maps when output_format == \"chunks\".",
		"json":          "Structured parser payload when output_format == \"json\" or unset.",
		"text":          "Plain-text payload when output_format == \"text\".",
		"markdown":      "Markdown payload when output_format == \"markdown\".",
		"html":          "HTML payload when output_format == \"html\".",
		"name":          "Upstream document name (used for title_tks and the title-blended embedding).",
	}
}

// Outputs 返回输出参数元数据，镜像 Python set_output 契约。
func (c *TokenizerComponent) Outputs() map[string]string {
	return map[string]string{
		"chunks":                      "Tokenized chunk list (each entry gains content_ltks / content_sm_ltks / title_tks and, when embedding is requested, q_<n>_vec).",
		"embedding_token_consumption": "Non-negative token count consumed by the embedding call. Omitted when no embedding ran.",
		"output_format":               "Always \"chunks\" (matches python set_output).",
		"_created_time":               "RFC3339Nano creation timestamp (TrackElapsed).",
		"_elapsed_time":               "Wall-clock seconds (TrackElapsed).",
	}
}

// Parallelism 固定为 1 — 嵌入调用单次批量往返（AD-5a）。
func (c *TokenizerComponent) Parallelism() int { return 1 }

// Invoke 为上游 chunks 计算 token 与嵌入。
//
// 失败模式：
//
//   - "embedding" requested but EncodeFunc is nil → returns an
//     error (fail-loud: same contract as python when LLMBundle is
//     unconstructable).
//   - Empty chunks list → returns an empty chunks output without
//     panicking (python tokenizer.py:121 treats this as valid).
//   - Per-chunk empty cleaned text → chunk is skipped from the
//     embedding batch (python tokenizer.py:80-82 `if not cleaned_txt:
//     continue`), but the chunk still carries tokenized fields if
//     `full_text` is in `search_method`.
func (c *TokenizerComponent) Invoke(ctx context.Context, inputs map[string]any) (map[string]any, error) {
	tenantID := getStringOr(inputs, "tenant_id", "")
	modelID := getStringOr(inputs, "model_id", "")
	upstream, err := decodeTokenizerFromUpstream(inputs)
	if err != nil {
		return nil, err
	}
	chunks := chunksFromTokenizerUpstream(upstream)
	name := upstream.Name
	titleStem := titleExtRE.ReplaceAllString(name, "")

	// TrackElapsed wraps the whole pipeline (tokenize + embed) so the
	// upstream caller sees consistent _created_time / _elapsed_time
	// stamps matching python `ProcessBase` (helpers.go TrackElapsed).
	return runtime.TrackElapsed("Tokenizer", func() (map[string]any, error) {
		// content_with_weight fallback — populate each chunk's
		// "text" from the python-equivalent field when empty.
		// Done before tokenizeChunks so the chunker's emitted
		// text is the authoritative input.
		normalizeChunkTextFallback(chunks)

		// full_text pass — tokenize each chunk's text fields. Mirrors
		// python tokenizer.py:130-185.
		if contains(c.param.SearchMethod, "full_text") {
			if err := tokenizeChunks(chunks, titleStem); err != nil {
				return nil, err
			}
		}

		out := map[string]any{
			"output_format": "chunks",
			"chunks":        schema.ChunkDocsToMaps(chunks),
		}

		// embedding pass — 单次批量调用（AD-5a）。
		if contains(c.param.SearchMethod, "embedding") {
			if EncodeFunc == nil {
				return nil, fmt.Errorf("Tokenizer: embedding requested but EncodeFunc is unset")
			}
			embedder := EncodeFunc(tenantID, modelID)
			if embedder == nil {
				return nil, fmt.Errorf("Tokenizer: embedding requested but encoder resolution returned nil")
			}

			// 构建批量文本列表与索引对。
			texts := make([]string, 0, len(chunks))
			pairs := make([]int, 0, len(chunks))
			for i, ck := range chunks {
				txt := concatFields(ck, c.param.Fields)
				txt = htmlTableRE.ReplaceAllString(txt, " ")
				txt = strings.TrimSpace(txt)
				if txt == "" {
					continue
				}
				texts = append(texts, txt)
				pairs = append(pairs, i)
			}

			if len(texts) > 0 {
				var (
					vects  [][]float64
					encErr error
				)
				timeoutErr := runtime.WithTimeout(ctx, tokenizerTimeout, func(timeoutCtx context.Context) error {
					vects, encErr = embedder.Encode(texts)
					return encErr
				})
				if timeoutErr != nil {
					return nil, fmt.Errorf("Tokenizer: encode: %w", timeoutErr)
				}
				if len(vects) != len(pairs) {
					return nil, fmt.Errorf("Tokenizer: encode returned %d vectors for %d chunks", len(vects), len(pairs))
				}
				for k, idx := range pairs {
					ck := &chunks[idx]
					v := vects[k]
					if err := ck.SetExtraValue(fmt.Sprintf("q_%d_vec", len(v)), v); err != nil {
						return nil, fmt.Errorf("Tokenizer: vector marshal: %w", err)
					}
				}
				// token_count: best-effort approximation matching the
				// python contract — the Go Embedder doesn't surface
				// per-call token usage, so we sum
				// `NumTokensFromString` for each chunk text.
				tokenCount := 0
				for _, t := range texts {
					tokenCount += tokenizer.NumTokensFromString(t)
				}
				out["embedding_token_consumption"] = tokenCount
				out["chunks"] = schema.ChunkDocsToMaps(chunks)
			}
		}

		return out, nil
	})
}

func decodeTokenizerFromUpstream(inputs map[string]any) (schema.TokenizerFromUpstream, error) {
	var out schema.TokenizerFromUpstream
	if inputs == nil {
		return out, fmt.Errorf("Tokenizer: inputs map is nil")
	}
	data, err := json.Marshal(stripRuntimeTimestamps(inputs))
	if err != nil {
		return out, fmt.Errorf("Tokenizer: encode inputs: %w", err)
	}
	if err := json.Unmarshal(data, &out); err != nil {
		return out, fmt.Errorf("Tokenizer: decode inputs: %w", err)
	}
	if err := out.Validate(); err != nil {
		return out, fmt.Errorf("Tokenizer: input error: %w", err)
	}
	return out, nil
}

func stripRuntimeTimestamps(inputs map[string]any) map[string]any {
	out := make(map[string]any, len(inputs))
	for k, v := range inputs {
		if k == "_created_time" || k == "_elapsed_time" {
			continue
		}
		out[k] = v
	}
	return out
}

func chunksFromTokenizerUpstream(in schema.TokenizerFromUpstream) []schema.ChunkDoc {
	switch in.OutputFormat {
	case schema.PayloadFormatChunks:
		return cloneChunkDocs(in.Chunks)
	case schema.PayloadFormatMarkdown:
		return textPayloadToChunks(in.MarkdownResult)
	case schema.PayloadFormatText:
		return textPayloadToChunks(in.TextResult)
	case schema.PayloadFormatHTML:
		return textPayloadToChunks(in.HTMLResult)
	default:
		return cloneChunkDocs(in.JSONResult)
	}
}

func textPayloadToChunks(payload *string) []schema.ChunkDoc {
	if payload == nil || strings.TrimSpace(*payload) == "" {
		return []schema.ChunkDoc{}
	}
	return []schema.ChunkDoc{{Text: *payload}}
}

func cloneChunkDocs(in []schema.ChunkDoc) []schema.ChunkDoc {
	if len(in) == 0 {
		return []schema.ChunkDoc{}
	}
	out := make([]schema.ChunkDoc, len(in))
	for i := range in {
		out[i] = cloneTokenizerChunkDoc(in[i])
	}
	return out
}

func cloneTokenizerChunkDoc(in schema.ChunkDoc) schema.ChunkDoc {
	out := in
	if in.TKNums != nil {
		v := *in.TKNums
		out.TKNums = &v
	}
	if in.ChunkOrderInt != nil {
		v := *in.ChunkOrderInt
		out.ChunkOrderInt = &v
	}
	if in.PageNumber != nil {
		v := *in.PageNumber
		out.PageNumber = &v
	}
	if in.Extra != nil {
		out.Extra = make(map[string]json.RawMessage, len(in.Extra))
		for k, v := range in.Extra {
			out.Extra[k] = append(json.RawMessage(nil), v...)
		}
	}
	if len(in.PDFPositions) > 0 {
		out.PDFPositions = append(json.RawMessage(nil), in.PDFPositions...)
	}
	if len(in.Positions) > 0 {
		out.Positions = append(json.RawMessage(nil), in.Positions...)
	}
	return out
}

// normalizeChunkTextFallback 在 text 缺失时从 content_with_weight 填充，镜像 tokenizer.py:111。
//
// The function mutates the input slice in place; callers should
// not retain separate copies of the chunks map. If both fields
// are present, the existing "text" wins — preserves the python
// contract where the chunker's emitted text is authoritative.
func normalizeChunkTextFallback(chunks []schema.ChunkDoc) {
	for i := range chunks {
		if chunks[i].Text != "" {
			continue
		}
		if chunks[i].ContentWithWeight != "" {
			chunks[i].Text = chunks[i].ContentWithWeight
		}
	}
}

// tokenizeChunks 为 chunk 标注 title_tks/content_ltks 及可选 question/keyword/summary 字段。
func tokenizeChunks(chunks []schema.ChunkDoc, titleStem string) error {
	for i := range chunks {
		ck := &chunks[i]
		ck.ChunkOrderInt = intPtr(i)
		titleTk, err := tokenizer.Tokenize(titleStem)
		if err != nil {
			return fmt.Errorf("Tokenizer: title tokenize: %w", err)
		}
		titleSmTk, err := tokenizer.FineGrainedTokenize(titleTk)
		if err != nil {
			return fmt.Errorf("Tokenizer: title fine-grain: %w", err)
		}
		ck.TitleTks = titleTk
		ck.TitleSmTks = titleSmTk

		// Question / keyword / summary fields are optional. The python
		// path branches on each independently.
		if q := ck.Questions; q != "" {
			if err := ck.SetExtraValue("question_kwd", strings.Split(q, "\n")); err != nil {
				return fmt.Errorf("Tokenizer: question keywords marshal: %w", err)
			}
			qt, err := tokenizer.Tokenize(q)
			if err != nil {
				return fmt.Errorf("Tokenizer: question tokenize: %w", err)
			}
			if err := ck.SetExtraValue("question_tks", qt); err != nil {
				return fmt.Errorf("Tokenizer: question tokens marshal: %w", err)
			}
		}
		if kw := ck.Keywords; kw != "" {
			if err := ck.SetExtraValue("important_kwd", strings.Split(kw, ",")); err != nil {
				return fmt.Errorf("Tokenizer: keyword list marshal: %w", err)
			}
			it, err := tokenizer.Tokenize(kw)
			if err != nil {
				return fmt.Errorf("Tokenizer: keyword tokenize: %w", err)
			}
			if err := ck.SetExtraValue("important_tks", it); err != nil {
				return fmt.Errorf("Tokenizer: keyword tokens marshal: %w", err)
			}
		}
		if s := ck.Summary; s != "" {
			st, err := tokenizer.Tokenize(s)
			if err != nil {
				return fmt.Errorf("Tokenizer: summary tokenize: %w", err)
			}
			ck.ContentLtks = st
			smt, err := tokenizer.FineGrainedTokenize(st)
			if err != nil {
				return fmt.Errorf("Tokenizer: summary fine-grain: %w", err)
			}
			ck.ContentSmLtks = smt
		} else if t := ck.Text; t != "" {
			tt, err := tokenizer.Tokenize(t)
			if err != nil {
				return fmt.Errorf("Tokenizer: text tokenize: %w", err)
			}
			ck.ContentLtks = tt
			smt, err := tokenizer.FineGrainedTokenize(tt)
			if err != nil {
				return fmt.Errorf("Tokenizer: text fine-grain: %w", err)
			}
			ck.ContentSmLtks = smt
		}
	}
	return nil
}

// concatFields 将 chunk 配置字段拼接为单字符串，镜像 tokenizer.py:69-79。
func concatFields(ck schema.ChunkDoc, fields []string) string {
	var b strings.Builder
	for _, f := range fields {
		switch f {
		case "text":
			b.WriteString(ck.Text)
		case "content_with_weight":
			b.WriteString(ck.ContentWithWeight)
		case "questions":
			b.WriteString(ck.Questions)
		case "keywords":
			b.WriteString(ck.Keywords)
		case "summary":
			b.WriteString(ck.Summary)
		default:
			if s, ok := ck.GetExtraString(f); ok {
				b.WriteString(s)
				continue
			}
			if values, ok := ck.GetExtraStringSlice(f); ok {
				b.WriteString(strings.Join(values, "\n"))
			}
		}
	}
	return b.String()
}

func getStringOr(m map[string]any, key, def string) string {
	if v, ok := getStringLocal(m, key); ok && v != "" {
		return v
	}
	return def
}

// getStringLocal 镜像 file.go 的 getString，避免 tokenizer 依赖 file 包。 Reads either a string or a byte slice (JSON
// decoding yields string for string fields by default).
func getStringLocal(m map[string]any, key string) (string, bool) {
	v, ok := m[key]
	if !ok || v == nil {
		return "", false
	}
	switch s := v.(type) {
	case string:
		return s, true
	case []byte:
		return string(s), true
	}
	return "", false
}

func contains(s []string, v string) bool {
	for _, x := range s {
		if x == v {
			return true
		}
	}
	return false
}

func intPtr(v int) *int { return &v }

// init 在 CategoryIngestion 下注册 Tokenizer，元数据驱动组件列表 API。
func init() {
	c := &TokenizerComponent{}
	runtime.MustRegister(ComponentNameTokenizer, runtime.CategoryIngestion,
		func(_ string, params map[string]any) (runtime.Component, error) {
			return NewTokenizerComponent(params)
		},
		runtime.Metadata{
			Version: "1.0.0",
			Inputs:  c.Inputs(),
			Outputs: c.Outputs(),
		})
}
// component/tokenizer.go — Tokenizer 组件实现：分词计数与批量嵌入。
