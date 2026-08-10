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

package chunker

import (
	"fmt"
	"regexp"
	"sort"
	"strings"

	"ragflow/internal/agent/runtime"
	"ragflow/internal/ingestion/component/schema"
	"ragflow/internal/tokenizer"
)

// newChunkerByName 将 DSL 名称分派到具体分块器构造函数。
// 集中于此，各 chunker 文件仅需 init 注册名称；直接返回 runtime.Component。
func newChunkerByName(name string, params map[string]any) (runtime.Component, error) {
	switch name {
	case ComponentNameTokenChunker:
		return NewTokenChunker(params)
	case ComponentNameTitleChunker:
		return NewTitleChunker(params)
	case ComponentNameGroupTitleChunker:
		return NewGroupTitleChunker(params)
	case ComponentNameHierarchyTitleChunker:
		return NewHierarchyTitleChunker(params)
	default:
		return nil, fmt.Errorf("chunker: unknown component %q", name)
	}
}

// ---------------------------------------------------------------------------
// 数值/列表转换辅助（各分块器变体共享）
// ---------------------------------------------------------------------------

// numericFromAny 将 JSON 解码整数规范为 float64，
// 使 schema 默认值与 Param.Update 约定不依赖编码来源。
func numericFromAny(v any) (float64, bool) {
	switch x := v.(type) {
	case float64:
		return x, true
	case float32:
		return float64(x), true
	case int:
		return float64(x), true
	case int32:
		return float64(x), true
	case int64:
		return float64(x), true
	case uint:
		return float64(x), true
	case uint32:
		return float64(x), true
	case uint64:
		return float64(x), true
	}
	return 0, false
}

func stringListFromAny(in []any) []string {
	out := make([]string, 0, len(in))
	for _, x := range in {
		if s, ok := x.(string); ok && s != "" {
			out = append(out, s)
		}
	}
	return out
}

// ---------------------------------------------------------------------------
// 正则/分割辅助
// ---------------------------------------------------------------------------

// compileDelimPattern 将分隔符合并为正则 alternation；
// 反引号包裹为字面量，普通字符串转义；较长模式优先。
func compileDelimPattern(delims []string) *regexp.Regexp {
	var custom []string
	var plain []string
	for _, d := range delims {
		if d == "" {
			continue
		}
		if strings.HasPrefix(d, "`") && strings.HasSuffix(d, "`") && len(d) >= 2 {
			custom = append(custom, regexp.QuoteMeta(d[1:len(d)-1]))
		} else {
			plain = append(plain, regexp.QuoteMeta(d))
		}
	}
	all := append(plain, custom...)
	if len(all) == 0 {
		return nil
	}
	sort.SliceStable(all, func(i, j int) bool { return len(all[i]) > len(all[j]) })
	return regexp.MustCompile(strings.Join(all, "|"))
}

// splitKeepingDelim 等价 Python re.split 并保留匹配分隔符，
// 与 token_chunker.py:88-93 重建逻辑无损对齐。
func splitKeepingDelim(text string, pattern *regexp.Regexp) []string {
	if pattern == nil {
		return []string{text}
	}
	idxs := pattern.FindAllStringSubmatchIndex(text, -1)
	if len(idxs) == 0 {
		return []string{text}
	}
	var out []string
	cursor := 0
	for _, idx := range idxs {
		start, end := idx[0], idx[1]
		if start > cursor {
			out = append(out, text[cursor:start])
		}
		out = append(out, text[start:end])
		cursor = end
	}
	if cursor < len(text) {
		out = append(out, text[cursor:])
	}
	return out
}

// ---------------------------------------------------------------------------
// chunk 文档辅助
// ---------------------------------------------------------------------------

// itemText 从 chunk 项取文本，优先 text 再 content_with_weight。
func itemText(it schema.ChunkDoc) (string, bool) {
	if it.Text != "" {
		return it.Text, true
	}
	if it.ContentWithWeight != "" {
		return it.ContentWithWeight, true
	}
	return "", false
}

// itemDocType 镜像 _build_json_chunks 的类型推导。
func itemDocType(it schema.ChunkDoc) string {
	switch strings.ToLower(strings.TrimSpace(it.DocType)) {
	case "table":
		return "table"
	case "image":
		return "image"
	}
	return "text"
}

// itemTextOrFallback 返回首选文本或空串。
func itemTextOrFallback(it schema.ChunkDoc) string {
	if t, ok := itemText(it); ok {
		return t
	}
	return ""
}

// tokenizeStr 共享 NumTokensFromString 包装，便于集中调整计数策略。
func tokenizeStr(s string) int { return tokenizer.NumTokensFromString(s) }

// toString 将 chunk-map 字段规范为字符串，缺失返回空。
func toString(v any) string {
	if v == nil {
		return ""
	}
	if s, ok := v.(string); ok {
		return s
	}
	return ""
}

// emptyOutputs 返回标准空 chunks 载荷。
func emptyOutputs() map[string]any {
	return map[string]any{
		"output_format": "chunks",
		"chunks":        []map[string]any{},
	}
}

func emptyChunkDocs() []schema.ChunkDoc { return []schema.ChunkDoc{} }

func chunkOutputs(chunks []schema.ChunkDoc) map[string]any {
	return map[string]any{
		"output_format": "chunks",
		"chunks":        schema.ChunkDocsToMaps(chunks),
	}
}
